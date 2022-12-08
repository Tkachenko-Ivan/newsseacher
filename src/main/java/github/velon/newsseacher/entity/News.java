package github.velon.newsseacher.entity;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "news")
public class News implements Serializable {

    @Id
    @SequenceGenerator(name = "news_id_seq", sequenceName = "news_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "news_id_seq")
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "headline", columnDefinition="text")
    String headline;

    @Column(name = "content_text", columnDefinition="text")
    String content;

    @Column(name = "post_date")
    LocalDate postDate;
    
    @Transient
    boolean active;
}
