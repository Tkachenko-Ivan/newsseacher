package github.velon.newsseacher.service;

import github.velon.newsseacher.entity.INDEXES;
import github.velon.newsseacher.entity.News;
import github.velon.newsseacher.repository.NewsRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service("NewsService")
public class NewsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    NewsRepository newsRepository;

    @Qualifier("manticore")
    @Autowired
    private JdbcTemplate manticoreJdbcTemplate;

    public Page<News> getAll(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    public Optional<News> getNews(Long id) {
        return newsRepository.findById(id);
    }

    @Transactional
    public News addNews(News news) {
        // Сохранение в хранилище
        News n = newsRepository.save(news);
        // Вставка в поисковый индекс
        insertToIndex(INDEXES.NEWS_RT, n, true);

        return n;
    }

    @Transactional
    public News updateNews(Long id, News news) {
        news.setId(id);
        News n = newsRepository.save(news);

        if (containsInIndex(INDEXES.NEWS_RT, id)) {
            deleteFromIndex(INDEXES.NEWS_RT, id);
        }
        insertToIndex(INDEXES.NEWS_RT, n, true);

        return n;
    }

    public void deleteNews(Long id) {
        newsRepository.deleteById(id);

        // Удалить из временного индекса
        deleteFromIndex(INDEXES.NEWS_RT, id);

        if (containsInIndex(INDEXES.NEWS_SQL, id)) {
            // Найти в основном индексе
            News news = getByIndex(INDEXES.NEWS_SQL, id);
            // Перекрыть запись
            insertToIndex(INDEXES.NEWS_DELETE, news, false);
        }
    }

    public List<News> find(String searchText) {
        String sql
                = """
                SELECT * 
                FROM news
                WHERE match (?) 
                ORDER BY active desc, weight() desc 
                """;
        List<News> result = manticoreJdbcTemplate.query(sql, (rs, rowNum) -> rsToNews(rs), searchText);
        result = result.stream().filter(n -> n.isActive()).collect(Collectors.toList());
        return result;
    }

    private News getByIndex(INDEXES index, Long id) {
        String sql = String.format("SELECT * FROM %s WHERE id = ?", index.getName());
        News result = manticoreJdbcTemplate.queryForObject(sql, (rs, rowNum) -> rsToNews(rs), id);
        return result;
    }

    /**
     * Вставляем данные в Real-Time индекс
     *
     * @param news вставляемые данные
     */
    private void insertToIndex(INDEXES index, News news, boolean actual) {
        try {
            String sql = String.format(
                    """
                    INSERT INTO %s (id, find_headline, find_content_text, headline, content_text, post_date, active)
                    VALUES (?,?,?,?,?,?,?)      
                    """,
                    index.getName());
            Long seconds = null;
            if (news.getPostDate() != null) {
                seconds = news.getPostDate().toEpochSecond(LocalTime.NOON, ZoneOffset.MIN);
            }
            manticoreJdbcTemplate.update(sql, news.getId(), news.getHeadline(), news.getContent(), news.getHeadline(), news.getContent(), seconds, actual);
        } catch (DataAccessException ex) {
            logger.error(ex.getMessage());
        }
    }

    private void deleteFromIndex(INDEXES index, long id) {
        try {
            String sql = String.format("DELETE FROM %s WHERE id = ?", index.getName());
            manticoreJdbcTemplate.update(sql, id);
        } catch (DataAccessException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Проверка наличия данных в Real-Time индексе
     *
     * @param id идентификатор записи
     * @return
     */
    private boolean containsInIndex(INDEXES index, long id) {
        String sql = String.format("SELECT count(*) FROM %s WHERE id = ?", index.getName());
        Integer count = manticoreJdbcTemplate.queryForObject(sql, Integer.class, id);
        return count > 0;
    }

    private News rsToNews(ResultSet rs) {
        try {
            News news = new News();
            news.setId(rs.getLong("id"));
            news.setHeadline(rs.getString("headline"));
            news.setContent(rs.getString("content_text"));
            news.setContent(rs.getString("content_text"));
            LocalDate date = Instant
                    .ofEpochMilli(rs.getLong("post_date") * 1000)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            news.setPostDate(date);
            news.setActive(rs.getBoolean("active"));
            return news;
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
            return null;
        }
    }
}
