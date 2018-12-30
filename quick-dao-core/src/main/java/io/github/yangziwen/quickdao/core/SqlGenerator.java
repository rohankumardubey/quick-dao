package io.github.yangziwen.quickdao.core;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.quickdao.core.util.ReflectionUtil;
import io.github.yangziwen.quickdao.core.util.StringWrapper;

public class SqlGenerator {

    private StringWrapper columnWrapper;

    private StringWrapper placeholderWrapper;

    public SqlGenerator(StringWrapper columnWrapper, StringWrapper placeholderWrapper) {
        this.columnWrapper = columnWrapper;
        this.placeholderWrapper = placeholderWrapper;
    }

    public <T> String generateUpdateSql(EntityMeta<T> entityMeta) {

        StringBuilder buff = new StringBuilder(" UPDATE ")
                .append(columnWrapper.wrap(entityMeta.getTable()));

        List<Field> fields = entityMeta.getFieldsWithoutIdField();

        buff.append(" SET ");

        buff.append(columnWrapper.wrap(entityMeta.getColumnNameByField(fields.get(0))))
            .append(" = ")
            .append(placeholderWrapper.wrap(fields.get(0).getName()));

        for (int i = 1; i < fields.size(); i++) {
            buff.append(", ")
                .append(columnWrapper.wrap(entityMeta.getColumnNameByField(fields.get(i))))
                .append(" = ")
                .append(placeholderWrapper.wrap(fields.get(i).getName()));
        }

        buff.append(" WHERE ").append(columnWrapper.wrap(entityMeta.getIdColumnName()))
            .append(" = ")
            .append(placeholderWrapper.wrap(entityMeta.getIdFieldName()));

        return buff.toString();
    }

    public <T> String generateUpdateSelectiveSql(EntityMeta<T> entityMeta, T entity) {

        StringBuilder buff = new StringBuilder(" UPDATE ")
                .append(columnWrapper.wrap(entityMeta.getTable()));

        buff.append(" SET ");

        int i = 0;

        for (Field field : entityMeta.getFieldsWithoutIdField()) {
            if (ReflectionUtil.getFieldValue(entity, field) == null) {
                continue;
            }
            if (i++ > 0) {
                buff.append(", ");
            }
            buff.append(columnWrapper.wrap(entityMeta.getColumnNameByField(field)))
                .append(" = ")
                .append(placeholderWrapper.wrap(field.getName()));
        }

        buff.append(" WHERE ").append(columnWrapper.wrap(entityMeta.getIdColumnName()))
            .append(" = ")
            .append(placeholderWrapper.wrap(entityMeta.getIdFieldName()));

        return buff.toString();
    }

    public <T> String generateInsertSql(EntityMeta<T> entityMeta) {

        StringBuilder buff = new StringBuilder(" INSERT INTO ")
                .append(columnWrapper.wrap(entityMeta.getTable()));

        List<Field> fields = entityMeta.getFieldsWithoutIdField();

        List<String> columnNames = entityMeta.getColumnNamesByFields(fields);

        buff.append(" ( ").append(columnWrapper.wrap(columnNames.get(0)));

        for (int i = 1; i < fields.size(); i++) {
            buff.append(", ").append(columnWrapper.wrap(columnNames.get(i)));
        }

        buff.append(" ) VALUES ( ").append(placeholderWrapper.wrap(fields.get(0).getName()));

        for (int i = 1; i < fields.size(); i++) {
            buff.append(", ").append(placeholderWrapper.wrap(fields.get(i).getName()));
        }

        buff.append(" ) ");

        return buff.toString();

    }

    public <T> String generateBatchInsertSql(EntityMeta<T> entityMeta, int batchSize) {

        List<Field> fields = entityMeta.getFieldsWithoutIdField();

        List<String> columnNames = entityMeta.getColumnNamesByFields(fields);

        StringBuilder buff = new StringBuilder().append(" INSERT INTO ")
                .append(columnWrapper.wrap(entityMeta.getTable()));

        buff.append(" ( ").append(columnWrapper.wrap(columnNames.get(0)));

        for (int i = 1; i < fields.size(); i++) {
            buff.append(", ").append(columnWrapper.wrap(columnNames.get(i)));
        }

        buff.append(" ) VALUES ");

        for (int i = 0; i < batchSize; i++) {
            buff.append("( ").append(placeholderWrapper.wrap(i, fields.get(0).getName()));
            for (int j = 1; j < columnNames.size(); j++) {
                buff.append(", ").append(placeholderWrapper.wrap(i, fields.get(j).getName()));
            }
            buff.append(" ) ");
            if (i < batchSize - 1) {
                buff.append(", ");
            }
        }

        return buff.toString();
    }

    public <T> String generateDeleteByPrimaryKeySql(EntityMeta<T> entityMeta) {
        return  " DELETE FROM " + columnWrapper.wrap(entityMeta.getTable()) +
                " WHERE " + columnWrapper.wrap(entityMeta.getIdColumnName()) +
                " = " + placeholderWrapper.wrap(entityMeta.getIdFieldName());
    }

    public <T> String generateDeleteByCriteriaSql(EntityMeta<T> entityMeta, Criteria criteria) {
        StringBuilder buff = new StringBuilder(" DELETE FROM " + columnWrapper.wrap(entityMeta.getTable()));
        appendWhere(buff, entityMeta, criteria);
        return buff.toString();
    }

    public <T> String generateListByQuerySql(EntityMeta<T> entityMeta, Query query) {
        StringBuilder buff = new StringBuilder();
        appendSelect(buff, entityMeta, query);
        appendFrom(buff, entityMeta);
        appendWhere(buff, entityMeta, query.getCriteria());
        appendGroupBy(buff, entityMeta, query);
        appendHaving(buff, entityMeta, query.getHavingCriteria());
        appendOrderBy(buff, entityMeta, query);
        appendLimit(buff, entityMeta, query);
        return buff.toString();
    }

    public <T> String generateCountByQuerySql(EntityMeta<T> entityMeta,Query query) {
        StringBuilder buff = new StringBuilder();
        buff.append(" SELECT COUNT(*) ");
        if (CollectionUtils.isNotEmpty(query.getGroupByList())) {
            buff.append(" FROM ( SELECT 1 ");
            appendFrom(buff, entityMeta);
            appendWhere(buff, entityMeta, query.getCriteria());
            appendGroupBy(buff, entityMeta, query);
            appendHaving(buff, entityMeta, query.getHavingCriteria());
            buff.append(" ) result ");
        } else {
            appendFrom(buff, entityMeta);
            appendWhere(buff, entityMeta, query.getCriteria());
        }
        return buff.toString();
    }

    private <T> void appendSelect(StringBuilder buff, EntityMeta<T> entityMeta, Query query) {
        buff.append(" SELECT ");
        if (CollectionUtils.isNotEmpty(query.getSelectStmtList())) {
            buff.append(query.getSelectStmtList().get(0));
            for (int i = 1; i < query.getSelectStmtList().size(); i++) {
                buff.append(", ").append(query.getSelectStmtList().get(i));
            }
            return;
        }
        int i = 0;
        for (String stmt : entityMeta.getSelectStmts(columnWrapper)) {
            if (i++ > 0) {
                buff.append(", ");
            }
            buff.append(stmt);
        }
    }

    private <T> void appendFrom(StringBuilder buff, EntityMeta<T> entityMeta) {
        buff.append(" FROM ").append(columnWrapper.wrap(entityMeta.getTable()));
    }

    private <T> void appendWhere(StringBuilder buff, EntityMeta<T> entityMeta, Criteria criteria) {
        if (criteria.isEmpty()) {
            return;
        }
        buff.append(" WHERE ");
        appendConditions(buff, entityMeta, criteria);
    }

    private <T> void appendConditions(StringBuilder buff, EntityMeta<T> entityMeta, Criteria criteria) {
        buff.append(" ( ");
        int i = 0;
        for (Criterion criterion : criteria.getCriterionList()) {
            if (i++ > 0) {
                buff.append(" AND ");
            }
            if (criterion.getValue() instanceof Object[]) {
                criterion.setValue(Arrays.asList((Object[]) criterion.getValue()));
            }
            buff.append(criterion.buildCondition(entityMeta, columnWrapper, placeholderWrapper));
        }
        buff.append(") ");
        for (Entry<String, Criteria> entry : criteria.getNestedCriteriaMap().entrySet()) {
            if (entry.getKey().endsWith(RepoKeys.OR)) {
                buff.append(" OR ");
                appendConditions(buff, entityMeta, entry.getValue());
            }
        }
    }

    private <T> void appendGroupBy(StringBuilder buff, EntityMeta<T> entityMeta, Query query) {
        if (CollectionUtils.isEmpty(query.getGroupByList())) {
            return;
        }
        buff.append(" GROUP BY ");
        int i = 0;
        for (String groupBy : query.getGroupByList()) {
            String columnName = entityMeta.getColumnNameByFieldName(groupBy);
            String stmt = StringUtils.isNotBlank(columnName)
                    ? columnWrapper.wrap(columnName) : groupBy;
            if (i++ > 0) {
                buff.append(", ");
            }
            buff.append(stmt);
        }
    }

    private <T> void appendHaving(StringBuilder buff, EntityMeta<T> entityMeta, Criteria criteria) {
        if (criteria.isEmpty()) {
            return;
        }
        buff.append(" HAVING ");
        appendConditions(buff, entityMeta, criteria);
    }

    private <T> void appendOrderBy(StringBuilder buff, EntityMeta<T> entityMeta, Query query) {
        if (CollectionUtils.isEmpty(query.getOrderList())) {
            return;
        }
        buff.append(" ORDER BY ");
        int i = 0;
        for (Order order : query.getOrderList()) {
            String columnName = entityMeta.getColumnNameByFieldName(order.getName());
            String stmt = StringUtils.isNotBlank(columnName)
                    ? columnWrapper.wrap(columnName) : order.getName();
            if (i++ > 0) {
                buff.append(",");
            }
            buff.append(stmt).append(" ").append(order.getDirection().name().toLowerCase());
        }
    }

    private <T> void appendLimit(StringBuilder buff, EntityMeta<T> entityMeta, Query query) {
        int offset = query.getOffset();
        int limit = query.getLimit();
        if (limit <= 0) {
            return;
        }
        if (offset < 0) {
            offset = 0;
        }
        buff.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
    }

}