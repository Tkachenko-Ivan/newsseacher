package github.velon.newsseacher.service;

import github.velon.newsseacher.entity.News;
import github.velon.newsseacher.repository.NewsRepository;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service("NewsService")
public class NewsService {

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

    public News addNews(News news) {
        return newsRepository.save(news);
    }

    public News updateNews(Long id, News news) {
        news.setId(id);
        return newsRepository.save(news);
    }

    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    public List<News> find(String searchText) {
        String sql
                = """
                SELECT * 
                FROM news_sql
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
}
