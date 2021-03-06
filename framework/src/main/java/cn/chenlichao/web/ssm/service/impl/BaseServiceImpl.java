/*
 * Copyright 2016-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.chenlichao.web.ssm.service.impl;

import cn.chenlichao.web.ssm.dao.entity.BaseEntity;
import cn.chenlichao.web.ssm.dao.mapper.BaseMapper;
import cn.chenlichao.web.ssm.exception.DAOException;
import cn.chenlichao.web.ssm.service.BaseService;
import cn.chenlichao.web.ssm.service.PageParams;
import cn.chenlichao.web.ssm.service.PageResults;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.ClassArrayEditor;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.Column;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 基础服务类实现
 *
 * <br>author: 陈立朝
 * <br>date: 16/6/12 上午11:10
 * <br>version: V1.0.0
 * <br>Copyright：Copyright © 2016 Chen Lichao. All rights reserved.
 */
public abstract class BaseServiceImpl<E extends BaseEntity<PK>, PK extends Serializable> implements BaseService<E, PK> {

    /** 日志 */
    protected Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected BaseMapper<E, PK> baseMapper;

    private Class<E> entityClass;
    private Class<PK> pkClass;

    @SuppressWarnings("unchecked")
    public BaseServiceImpl() {
        // 返回此 Class 所表示的实体 (类, 接口, 基本类型或void) 的直接超类的 Type
        Type genType = getClass().getGenericSuperclass();

        // 逐级向上查找, 直到找到对应的泛型类型, 用于处理多级继承
        while(!genType.equals(Object.class)) {
            if (genType instanceof ParameterizedType) {
                ParameterizedType superType = (ParameterizedType) genType;
                Type[] params = superType.getActualTypeArguments();
                if (params.length == 2 && params[0] instanceof Class) {
                    entityClass = (Class<E>) params[0];
                    pkClass = (Class<PK>) params[1];
                    break;
                }
            }
            if (genType instanceof Class) {
                genType = ((Class) genType).getGenericSuperclass();
            } else {
                break;
            }
        }
        LOGGER.debug("Entity Class: {}, PK class: {}", entityClass.getSimpleName(), pkClass.getSimpleName());
    }

    @Autowired
    public void setBaseMapper(BaseMapper<E, PK> baseMapper) {
        this.baseMapper = baseMapper;
    }

    @Override
    public PK save(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("数据实体为null");
        }
        int result = baseMapper.insert(entity);
        if (result != 1) {
            throw new PersistenceException("数据插入失败");
        }
        LOGGER.trace("数据库插入成功, 新ID: [{}]", entity.getId());
        return entity.getId();
    }

    @Override
    public int save(Collection<E> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("数据实体集合为null");
        }
        int successed = 0;
        for (E entity : entities) {
            try {
                save(entity);
                successed++;
            } catch (Exception e) {
                LOGGER.error("批量插入数据失败: [{}]", entity);
            }
        }
        return successed;
    }

    @Override
    public PK saveWithNull(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("数据实体为null");
        }
        int result = baseMapper.insertWithNull(entity);
        if (result != 1) {
            throw new PersistenceException("数据插入失败");
        }
        LOGGER.trace("数据库插入成功, 新ID: [{}]", entity.getId());
        return entity.getId();
    }

    @Override
    public int saveWithNull(Collection<E> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("数据实体集合为null");
        }
        int successed = 0;
        for (E entity : entities) {
            try {
                saveWithNull(entity);
                successed++;
            } catch (Exception e) {
                LOGGER.error("批量插入数据失败: [{}]", entity);
            }
        }
        return successed;
    }

    @Override
    public int update(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("数据实体为null");
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("数据实体ID为null");
        }
        return baseMapper.update(entity);
    }

    @Override
    public int updateWithNull(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("数据实体为null");
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("数据实体ID为null");
        }
        return baseMapper.updateWithNull(entity);
    }

    @Override
    public int updateByExample(Example example, E entity) {
        if (example == null) {
            throw new IllegalArgumentException("查询条件为null");
        }
        if (entity == null) {
            throw new IllegalArgumentException("数据实体为null");
        }
        if (example.getEntityClass() == null || !entity.getClass().equals(example.getEntityClass())) {
            throw new IllegalArgumentException("查询条件实体类与数据实体类型不一致");
        }
        return baseMapper.updateByExample(entity, example);
    }

    @Override
    public int updateByExampleWithNull(Example example, E entity) {
        if (example == null) {
            throw new IllegalArgumentException("查询条件为null");
        }
        if (entity == null) {
            throw new IllegalArgumentException("数据实体为null");
        }
        if (example.getEntityClass() == null || !entity.getClass().equals(example.getEntityClass())) {
            throw new IllegalArgumentException("查询条件实体类与数据实体类型不一致");
        }
        return baseMapper.updateByExampleWithNull(entity, example);
    }

    @Override
    public int delete(PK id) {
        if (id == null) {
            throw new IllegalArgumentException("ID为null");
        }
        return baseMapper.deleteById(id);
    }

    @Override
    public int delete(Collection<PK> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("ID集合为null");
        }
        int successed = 0;
        for (PK id : ids) {
            try {
                delete(id);
                successed++;
            } catch (Exception e) {
                LOGGER.error("批量删除数据失败: [{}]", id);
            }
        }
        return successed;
    }

    @Override
    public int delete(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("条件实体为null");
        }
        return baseMapper.delete(entity);
    }

    @Override
    public int deleteByExample(Example example) {
        if (example == null) {
            throw new IllegalArgumentException("删除条件为null");
        }
        if (entityClass != null && !entityClass.equals(example.getEntityClass())) {
            throw new IllegalArgumentException("查询条件实体类与服务类型不匹配");
        }
        return baseMapper.deleteByExample(example);
    }

    @Override
    public E get(PK id) {
        if (id == null) {
            throw new IllegalArgumentException("ID为null");
        }
        return baseMapper.get(id);
    }

    @Override
    public List<E> select(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("条件实体为null");
        }
        return baseMapper.select(entity);
    }

    @Override
    public List<E> selectByExample(Example example) {
        if (example == null) {
            throw new IllegalArgumentException("查询条件为null");
        }
        if (entityClass != null && !entityClass.equals(example.getEntityClass())) {
            throw new IllegalArgumentException("查询条件实体类与服务类型不匹配");
        }
        return baseMapper.selectByExample(example);
    }

    @Override
    public List<E> selectAll() {
        return baseMapper.selectAll();
    }

    @Override
    public int selectCount(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("条件实体为null");
        }
        return baseMapper.selectCount(entity);
    }

    @Override
    public int selectCountByExample(Example example) {
        if (example == null) {
            throw new IllegalArgumentException("查询条件为null");
        }
        if (entityClass != null && !entityClass.equals(example.getEntityClass())) {
            throw new IllegalArgumentException("查询条件实体类与服务类型不匹配");
        }
        return baseMapper.selectCountByExample(example);
    }

    @Override
    public PageResults<E> queryPage(PageParams<E> pageParams) {
        if (pageParams == null) {
            throw new IllegalArgumentException("分页参数为null");
        }
        LOGGER.trace("执行分页查询...");

        // 初始化参数
        int pageNum = pageParams.getPageIndex();
        int pageSize = pageParams.getPageSize();
        if (pageSize > 100) {
            LOGGER.warn("分页查询, 页大小为[{}] 条, 重置为 100 条。", pageSize);
            pageSize = 100;
        }

        // 设置分页信息
        PageHelper.startPage(pageNum, pageSize);

        Example example = new Example(entityClass);
        // 处理参数
        E params = pageParams.getParamEntity();
        if (params != null) {
            Example.Criteria criteria = example.createCriteria();
            PropertyDescriptor[] propArray = BeanUtils.getPropertyDescriptors(entityClass);
            for (PropertyDescriptor pd : propArray) {
                if (pd.getPropertyType().equals(Class.class)) {
                    continue;
                }
                try {
                    Object value = pd.getReadMethod().invoke(params);
                    if (value != null) {
                        if (pd.getPropertyType() == String.class) {
                            String strValue = (String) value;
                            if (strValue.startsWith("%") || strValue.endsWith("%")) {
                                criteria.andLike(pd.getName(), strValue);
                                continue;
                            }
                        }
                        criteria.andEqualTo(pd.getName(), value);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOGGER.error("构建分页查询example时出错: {}", e.getMessage(), e);
                }
            }
        }
        // 处理排序
        String orderBy = pageParams.getOrderBy();
        if (StringUtils.hasText(orderBy)) {
            processOrder(example, orderBy, pageParams.isAsc());
        }
        List<E> results = baseMapper.selectByExample(example);

        // 返回空结果集
        if (results == null || !(results instanceof Page)) {
            return new PageResults<>(0, new ArrayList<E>(0), pageParams);
        }
        Page page = (Page)results;
        Long totalCount = page.getTotal();
        return new PageResults<>(totalCount.intValue(), Collections.unmodifiableList(results), pageParams);
    }

    @Override
    public PageResults<E> queryPageByExample(PageParams<E> pageParams, Example example) {
        if(pageParams == null) {
            throw new IllegalArgumentException("分页参数为null");
        }
        if(example == null) {
            throw new IllegalArgumentException("查询条件为null");
        }
        if (entityClass != null && !entityClass.equals(example.getEntityClass())) {
            throw new IllegalArgumentException("查询条件实体类与服务实体类型不匹配");
        }

        LOGGER.trace("开始进行example条件分页查询...");
        // 处理参数
        int pageNum = pageParams.getPageIndex();
        int pageSize = pageParams.getPageSize();
        if (pageSize > 100) {
            LOGGER.warn("分页查询, 页大小为[{}] 条, 重置为 100 条。", pageSize);
            pageSize = 100;
        }

        // 设定分页
        PageHelper.startPage(pageNum, pageSize);
        List<E> results = baseMapper.selectByExample(example);
        // 返回空结果集
        if (results == null || !(results instanceof Page)) {
            return new PageResults<>(0, new ArrayList<E>(0), pageParams);
        }
        Page page = (Page)results;
        Long totalCount = page.getTotal();
        return new PageResults<>(totalCount.intValue(), results, pageParams);
    }

    private void processOrder(Example example, String orderBy, boolean isAsc) {
        Class<?> clazz = example.getEntityClass();
        try {
            Field field = clazz.getDeclaredField(orderBy);
            if (field != null) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    example.setOrderByClause(column.name() + (isAsc ? " ASC" : " DESC"));
                    return;
                }
            }
        } catch (NoSuchFieldException e) {
            // do nothing
        }
        example.setOrderByClause(orderBy + (isAsc ? " ASC" : " DESC"));
    }
}
