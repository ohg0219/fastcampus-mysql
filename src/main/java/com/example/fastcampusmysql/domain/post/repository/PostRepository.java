package com.example.fastcampusmysql.domain.post.repository;

import com.example.fastcampusmysql.domain.member.entity.Member;
import com.example.fastcampusmysql.util.PageHelper;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.Optional;

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
            .likeCount(resultSet.getLong("likeCount"))
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
        return update(post);
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
                    ORDER BY %s
                    LIMIT :size
                    OFFSET :offset
                """, TABLE, PageHelper.orderBy(pageable.getSort()));

        var posts = namedJdbcTemplate.query(sql, params, ROW_MAPPER);

        return new PageImpl<>(posts, pageable, getCount(memberId));
    }

    public Optional<Post> findById(Long postId, Boolean requiredLock) {
        var sql = String.format("SELECT * FROM %s WHERE id = :postId ", TABLE);

        if (requiredLock) {
            sql += "FOR UPDATE";
        }
        var params = new MapSqlParameterSource().addValue("postId", postId);
        var nullablePost = namedJdbcTemplate.queryForObject(sql, params, ROW_MAPPER);
        return Optional.ofNullable(nullablePost);
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


    public List<Post> findAllByMemberIdAndOrderByIdDesc(Long memberId, int size) {
        var sql = String.format("""
                    SELECT *
                    FROM %s
                    WHERE memberId = :memberId
                    ORDER BY id DESC
                    LIMIT :size
                """, TABLE);

        var params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("size", size);

        return namedJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByInMemberIdAndOrderByIdDesc(List<Long> memberIds, int size) {
        if (memberIds.isEmpty()) {
            return List.of();
        }

        var sql = String.format("""
                    SELECT *
                    FROM %s
                    WHERE memberId IN (:memberIds)
                    ORDER BY id DESC
                    LIMIT :size
                """, TABLE);

        var params = new MapSqlParameterSource()
                .addValue("memberIds", memberIds)
                .addValue("size", size);

        return namedJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByInId(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        var sql = String.format("""
                    SELECT *
                    FROM %s
                    WHERE id in (:ids)
                """, TABLE);

        var params = new MapSqlParameterSource()
                .addValue("ids", ids);

        return namedJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByLessThanIdAndMemberIdAndOrderByIdDesc(Long id, Long memberId, int size) {
        var sql = String.format("""
                    SELECT *
                    FROM %s
                    WHERE memberId = :memberId and id < :id
                    ORDER BY id DESC
                    LIMIT :size
                """, TABLE);

        var params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("size", size)
                .addValue("id", id);

        return namedJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByLessThanIdAndInMemberIdAndOrderByIdDesc(Long id, List<Long> memberIds, int size) {
        if (memberIds.isEmpty()) {
            return List.of();
        }

        var sql = String.format("""
                    SELECT *
                    FROM %s
                    WHERE memberId IN (:memberIds) and id < :id
                    ORDER BY id DESC
                    LIMIT :size
                """, TABLE);

        var params = new MapSqlParameterSource()
                .addValue("memberIds", memberIds)
                .addValue("size", size)
                .addValue("id", id);

        return namedJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    private Post update(Post post) {
        var sql = String.format("""
                UPDATE %s SET 
                  memberId = :memberId,
                  contents = :contents,
                  createdDate = :createdDate,
                  likeCount = :likeCount,
                  createdAt = :createdAt
                  WHERE id = :id
                """, TABLE);
        SqlParameterSource params = new BeanPropertySqlParameterSource(post);
        namedJdbcTemplate.update(sql, params);
        return post;
    }

}
