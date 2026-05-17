package com.myforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myforum.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
