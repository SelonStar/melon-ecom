package com.melonecom.mapper;

import com.melonecom.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<Long> selectEnabledUserIds();
}
