package github.velon.newsseacher.controller;

import github.velon.newsseacher.entity.News;
import github.velon.newsseacher.service.NewsService;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RequestMapping(value = "news")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NewsController {

    NewsService service;

    @GetMapping
    public Page<News> getAllNews(Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping(value = "/{id}")
    public Optional<News> getNews(@PathVariable Long id) {
        return service.getNews(id);
    }

    @PostMapping
    public News addNews(@RequestBody News news) {
        return service.addNews(news);
    }

    @PutMapping(value = "/{id}")
    public News updateNews(
            @PathVariable Long id,
            @RequestBody News news) throws Exception {
        return service.updateNews(id, news);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteNews(@PathVariable Long id) throws Exception {
        service.deleteNews(id);
    }

    @GetMapping(value = "find")
    public List<News> find(@RequestParam String text) {
        return service.find(text);
    }
}
