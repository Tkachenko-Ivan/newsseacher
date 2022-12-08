package github.velon.newsseacher.entity;

/**
 * Список допустимых индексов
 *
 * @author Иван
 */
public enum INDEXES {
    NEWS("news"),
    NEWS_SQL("news_sql"),
    NEWS_RT("news_rt"),
    NEWS_DELETE("news_delete");

    private String name;

    INDEXES(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
