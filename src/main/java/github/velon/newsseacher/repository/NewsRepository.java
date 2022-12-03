package github.velon.newsseacher.repository;

import github.velon.newsseacher.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("NewsRepository")
public interface NewsRepository extends JpaRepository<News, Long> {
}
