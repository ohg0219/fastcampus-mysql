package com.example.fastcampusmysql.domain.post.repository;

import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    private static final String TABLE = "post";

    private static final RowMapper<Post> ROW_MAPPER = (ResultSet resultSet, int rowNum)
            -> Post.builder()
            .id(resultSet.getLong("id"))
            .memberId(resultSet.getLong("memberId"))
            .contents(resultSet.getString("contents"))
            .createdDate(resultSet.getObject("createdDate", LocalDate.class))
            .createdAt(resultSet.getObject("createdAt", LocalDateTime.class))
            .build();

    private static final RowMapper<DailyPostCount> GROUP_BY_ROW_MAPPER = (ResultSet resultSet, int rowNum)
            -> new DailyPostCount(
            resultSet.getLong("memberId"),
            resultSet.getObject("createdDate", LocalDate.class),
            resultSet.getLong("count")
    );


    public Post save(Post post) {
        if (post.getId() == null) {
            return insert(post);
        }
        throw new UnsupportedOperationException("post 는 갱신을 지원하지 않습니다");
    }

    private Post insert(Post post) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(namedJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");
        SqlParameterSource params = new BeanPropertySqlParameterSource(post);
        var id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Post.builder()
                .id(id)
                .memberId(post.getMemberId())
                .contents(post.getContents())
                .createdDate(post.getCreatedDate())
                .createdAt(post.getCreatedAt())
                .build();
    }

    public void bulkInsert(List<Post> posts) {
        var sql = """
                INSERT INTO %s (memberId, contents, createdDate, createdAt)
                VALUES (:memberId, :contents, :createdDate, :createdAt)
                """.formatted(TABLE);

        SqlParameterSource[] params = posts
                .stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        namedJdbcTemplate.batchUpdate(sql, params);
    }

    public List<DailyPostCount> groupByCreatedDate(DailyPostCountRequest request) {
        var sql = """
                SELECT createdDate, memberId, COUNT(id) as count
                FROM %s
                WHERE memberId = :memberId AND createdDate BETWEEN :firstDate and :lastDate
                GROUP BY memberId, createdDate
                """.formatted(TABLE);
        var params = new BeanPropertySqlParameterSource(request);
        return namedJdbcTemplate.query(sql, params, GROUP_BY_ROW_MAPPER);
    }


    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable) {
        var params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("size", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        var sql = String.format("""
                    SELECT *
                    FROM %s
                    WHERE memberId = :memberId
                    LIMIT :size
                    OFFSET :offset
                """, TABLE);

        var posts = namedJdbcTemplate.query(sql, params, ROW_MAPPER);

        return new PageImpl<>(posts, pageable, getCount(memberId));

    }

    private Long getCount(Long memberId) {
        var sql = String.format("""
                    SELECT COUNT(*)
                    FROM %s
                    WHERE memberId = :memberId
                """, TABLE);

        var params = new MapSqlParameterSource()
                .addValue("memberId", memberId);

        return namedJdbcTemplate.queryForObject(sql, params, Long.class);
    }

}
