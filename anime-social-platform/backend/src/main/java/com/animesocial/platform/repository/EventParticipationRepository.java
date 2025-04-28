package com.animesocial.platform.repository;

import com.animesocial.platform.model.EventParticipation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

/**
 * 活动参与数据访问类
 * 负责活动参与相关数据库操作
 */
@Repository
public class EventParticipationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * EventParticipation行映射器
     */
    private final RowMapper<EventParticipation> rowMapper = (rs, rowNum) -> {
        EventParticipation participation = new EventParticipation();
        participation.setId(rs.getInt("id"));
        participation.setUserId(rs.getInt("user_id"));
        participation.setEventId(rs.getInt("event_id"));
        participation.setParticipationTime(rs.getTimestamp("participation_time").toLocalDateTime());
        participation.setStatus(rs.getInt("status"));
        return participation;
    };

    /**
     * 添加活动参与记录
     * @param participation 活动参与对象
     * @return 新增记录ID
     */
    public int insert(EventParticipation participation) {
        String sql = "INSERT INTO event_participations (user_id, event_id, participation_time, status) VALUES (?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, participation.getUserId());
            ps.setInt(2, participation.getEventId());
            ps.setTimestamp(3, Timestamp.valueOf(participation.getParticipationTime()));
            ps.setInt(4, participation.getStatus() != null ? participation.getStatus() : 1);
            return ps;
        }, keyHolder);
        
        return keyHolder.getKey().intValue();
    }

    /**
     * 根据用户ID查询参与的活动ID列表
     * @param userId 用户ID
     * @return 活动ID列表
     */
    public List<Integer> findEventIdsByUserId(Integer userId) {
        String sql = "SELECT event_id FROM event_participations WHERE user_id = ? AND status = 1";
        return jdbcTemplate.queryForList(sql, Integer.class, userId);
    }

    /**
     * 根据活动ID查询参与的用户ID列表
     * @param eventId 活动ID
     * @return 用户ID列表
     */
    public List<Integer> findUserIdsByEventId(Integer eventId) {
        String sql = "SELECT user_id FROM event_participations WHERE event_id = ? AND status = 1";
        return jdbcTemplate.queryForList(sql, Integer.class, eventId);
    }

    /**
     * 检查用户是否参与了指定活动
     * @param userId 用户ID
     * @param eventId 活动ID
     * @return 是否参与
     */
    public boolean exists(Integer userId, Integer eventId) {
        String sql = "SELECT COUNT(1) FROM event_participations WHERE user_id = ? AND event_id = ? AND status = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, eventId);
        return count != null && count > 0;
    }

    /**
     * 删除活动参与记录
     * @param userId 用户ID
     * @param eventId 活动ID
     * @return 影响行数
     */
    public int delete(Integer userId, Integer eventId) {
        String sql = "UPDATE event_participations SET status = 0 WHERE user_id = ? AND event_id = ?";
        return jdbcTemplate.update(sql, userId, eventId);
    }

    /**
     * 统计活动参与人数
     * @param eventId 活动ID
     * @return 参与人数
     */
    public int countParticipants(Integer eventId) {
        String sql = "SELECT COUNT(1) FROM event_participations WHERE event_id = ? AND status = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, eventId);
        return count != null ? count : 0;
    }

    /**
     * 根据ID查询活动参与记录
     * @param id 记录ID
     * @return 活动参与记录，如果不存在返回null
     */
    public EventParticipation findById(Integer id) {
        String sql = "SELECT * FROM event_participations WHERE id = ?";
        List<EventParticipation> participations = jdbcTemplate.query(sql, rowMapper, id);
        return participations.isEmpty() ? null : participations.get(0);
    }
} 