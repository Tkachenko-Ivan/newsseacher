package github.velon.newsseacher.service;

import github.velon.newsseacher.entity.News;
import github.velon.newsseacher.repository.NewsRepository;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service("NewsService")
public class NewsService {

    NewsRepository newsRepository;

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
}
