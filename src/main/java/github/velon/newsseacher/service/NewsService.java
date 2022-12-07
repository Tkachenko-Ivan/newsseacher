package github.velon.newsseacher.service;

import github.velon.newsseacher.entity.News;
import github.velon.newsseacher.repository.NewsRepository;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
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
        insertToIndex(n);

        return n;
    }

    @Transactional
    public News updateNews(Long id, News news) {
        news.setId(id);
        News n = newsRepository.save(news);

        if (containsInIndex(id)) {
            updateInIndex(n);
        } else {
            // Вставка
            insertToIndex(n);
        }

        return n;
    }

    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    public List<News> find(String searchText) {
        String sql
                = """
                SELECT * 
                FROM news
                WHERE match (?) 
                ORDER BY weight() desc 
                """;
        List<News> result = manticoreJdbcTemplate.query(sql, (rs, rowNum) -> {
            try {
                News news = new News();
                news.setId(rs.getLong("id"));
                news.setHeadline(rs.getString("headline"));
                news.setContent(rs.getString("content_text"));
                LocalDate date = Instant
                        .ofEpochMilli(rs.getLong("post_date") * 1000)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                news.setPostDate(date);
                return news;
            } catch (SQLException ex) {
                return null;
            }
        }, searchText);
        return result;
    }

    /**
     * Вставляем данные в Real-Time индекс
     *
     * @param news вставляемые данные
     */
    private void insertToIndex(News news) {
        try {
            String sql = """
                     INSERT INTO news_rt (id, find_headline, find_content_text, headline, content_text, post_date)
                     VALUES (?,?,?,?,?,?)      
                     """;
            Long seconds = null;
            if (news.getPostDate() != null) {
                seconds = news.getPostDate().toEpochSecond(LocalTime.NOON, ZoneOffset.MIN);
            }
            manticoreJdbcTemplate.update(sql, news.getId(), news.getHeadline(), news.getContent(), news.getHeadline(), news.getContent(), seconds);
        } catch (DataAccessException ex) {
            logger.error(ex.getMessage());
        }
    }

    private void updateInIndex(News news) {
        try {
            String sql = """
                     UPDATE news_rt
                     SET find_headline = ?, find_content_text = ?, headline = ?, content_text = ?, post_date = ?
                     WHERE id = ?   
                     """;
            Long seconds = null;
            if (news.getPostDate() != null) {
                seconds = news.getPostDate().toEpochSecond(LocalTime.NOON, ZoneOffset.MIN);
            }
            manticoreJdbcTemplate.update(sql, news.getHeadline(), news.getContent(), news.getHeadline(), news.getContent(), seconds, news.getId());
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
    private boolean containsInIndex(long id) {
        String sql = "SELECT count(*) FROM news_rt WHERE id = ?";
        Integer count = manticoreJdbcTemplate.queryForObject(sql, Integer.class, id);
        return count > 0;
    }
}
