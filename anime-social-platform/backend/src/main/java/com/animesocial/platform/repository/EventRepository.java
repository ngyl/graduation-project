package com.animesocial.platform.repository;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.*;

import com.animesocial.platform.model.Event;

/**
 * 活动数据访问接口
 * 使用MyBatis注解方式定义SQL操作，处理活动相关的数据库操作
 */
@Mapper
public interface EventRepository {
    
    /**
     * 根据ID查询活动
     * @param id 活动ID
     * @return 活动对象，如果不存在则返回null
     */
    @Select("SELECT * FROM events WHERE id = #{id}")
    Event findById(Integer id);
    
    /**
     * 根据条件查询活动列表
     * @param status 活动状态(可选)
     * @param startTime 开始时间(可选)
     * @param endTime 结束时间(可选)
     * @return 活动列表，按开始时间升序排序
     */
    @Select({
        "<script>",
        "SELECT * FROM events",
        "<where>",
        "  <if test='status != null'>",
        "    AND status = #{status}",
        "  </if>",
        "  <if test='startTime != null'>",
        "    AND start_time &gt;= #{startTime}",
        "  </if>",
        "  <if test='endTime != null'>",
        "    AND end_time &lt;= #{endTime}",
        "  </if>",
        "</where>",
        "ORDER BY start_time ASC",
        "</script>"
    })
    List<Event> findAllByFilter(@Param("status") Integer status, @Param("startTime") LocalDate startTime, @Param("endTime") LocalDate endTime);
    
    /**
     * 查询当前进行中的活动
     * @param now 当前时间
     * @return 活动列表，按开始时间升序排序
     */
    @Select("SELECT * FROM events WHERE status = 1 AND start_time <= #{now} AND end_time >= #{now} ORDER BY start_time ASC")
    List<Event> findCurrentEvents(LocalDate now);
    
    /**
     * 查询即将开始的活动
     * @param now 当前时间
     * @param future 未来时间点
     * @return 活动列表，按开始时间升序排序
     */
    @Select("SELECT * FROM events WHERE status = 1 AND start_time > #{now} AND start_time <= #{future} ORDER BY start_time ASC")
    List<Event> findUpcomingEvents(LocalDate now, LocalDate future);
    
    /**
     * 插入活动
     * @param event 活动对象
     * @return 插入成功返回1，失败返回0
     */
    @Insert("INSERT INTO events (title, description, start_time, end_time, status, created_by, created_at) " +
            "VALUES (#{title}, #{description}, #{startTime}, #{endTime}, #{status}, #{createdBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Event event);
    
    /**
     * 更新活动
     * @param event 活动对象
     * @return 更新成功返回1，失败返回0
     */
    @Update("UPDATE events SET title = #{title}, description = #{description}, start_time = #{startTime}, end_time = #{endTime} WHERE id = #{id}")
    void update(Event event);
    
    /**
     * 更新活动状态
     * @param id 活动ID
     * @param status 活动状态(0下线,1上线)
     * @return 更新成功返回1，失败返回0
     */
    @Update("UPDATE events SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    
    /**
     * 删除活动
     * @param id 活动ID
     * @return 删除成功返回1，失败返回0
     */
    @Delete("DELETE FROM events WHERE id = #{id}")
    void delete(Integer id);
    
    /**
     * 统计活动总数
     * @return 活动总数
     */
    @Select("SELECT COUNT(*) FROM events")
    int count();
    
    /**
     * 根据状态统计活动数量
     * @param status 活动状态(可选)
     * @return 活动数量
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM events",
        "<where>",
        "  <if test='status != null'>",
        "    status = #{status}",
        "  </if>",
        "</where>",
        "</script>"
    })
    int countByStatus(@Param("status") Integer status);
    
    /**
     * 根据ID列表批量查询活动
     * @param ids 活动ID列表
     * @return 活动列表，按开始时间升序排序
     */
    @Select({
        "<script>",
        "SELECT * FROM events",
        "<where>",
        "  <if test='ids != null and ids.size() > 0'>",
        "    id IN ",
        "    <foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "      #{id}",
        "    </foreach>",
        "  </if>",
        "</where>",
        "ORDER BY start_time ASC",
        "</script>"
    })
    List<Event> findAllByIds(@Param("ids") List<Integer> ids);
} 