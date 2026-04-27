package com.example.wanlvback.service.impl;

import com.example.wanlvback.pojo.vo.DatabaseSchemaVO;
import com.example.wanlvback.pojo.vo.SchemaColumnVO;
import com.example.wanlvback.pojo.vo.SchemaTableVO;
import com.example.wanlvback.service.InternalSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 内部表结构服务实现
 */
@Service
@Slf4j
public class InternalSchemaServiceImpl implements InternalSchemaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public DatabaseSchemaVO getSchema(List<String> tableNames) {
        String databaseName = jdbcTemplate.queryForObject("select database()", String.class);
        List<String> normalizedTableNames = normalizeTableNames(tableNames);

        Map<String, SchemaTableVO> tableMap = loadTables(databaseName, normalizedTableNames);
        loadColumns(databaseName, normalizedTableNames, tableMap);

        log.info("完成内部表结构查询, databaseName={}, tableCount={}", databaseName, tableMap.size());
        return DatabaseSchemaVO.builder()
                .databaseName(databaseName)
                .generatedAt(LocalDateTime.now())
                .tables(new ArrayList<>(tableMap.values()))
                .build();
    }

    private Map<String, SchemaTableVO> loadTables(String databaseName, List<String> tableNames) {
        StringBuilder sql = new StringBuilder("""
                select table_name, table_comment
                from information_schema.tables
                where table_schema = ?
                  and table_type = 'BASE TABLE'
                """);

        List<Object> params = new ArrayList<>();
        params.add(databaseName);
        appendTableNameFilter(sql, params, tableNames);
        sql.append(" order by table_name");

        return jdbcTemplate.query(sql.toString(), rs -> {
            Map<String, SchemaTableVO> result = new LinkedHashMap<>();
            while (rs.next()) {
                SchemaTableVO table = SchemaTableVO.builder()
                        .tableName(rs.getString("table_name"))
                        .tableComment(rs.getString("table_comment"))
                        .columns(new ArrayList<>())
                        .build();
                result.put(table.getTableName(), table);
            }
            return result;
        }, params.toArray());
    }

    private void loadColumns(String databaseName, List<String> tableNames, Map<String, SchemaTableVO> tableMap) {
        if (tableMap.isEmpty()) {
            return;
        }

        StringBuilder sql = new StringBuilder("""
                select table_name,
                       column_name,
                       data_type,
                       column_type,
                       is_nullable,
                       column_default,
                       column_key,
                       extra,
                       column_comment,
                       ordinal_position
                from information_schema.columns
                where table_schema = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(databaseName);
        appendTableNameFilter(sql, params, tableNames);
        sql.append(" order by table_name, ordinal_position");

        jdbcTemplate.query(sql.toString(), rs -> {
            while (rs.next()) {
                SchemaTableVO table = tableMap.get(rs.getString("table_name"));
                if (table == null) {
                    continue;
                }

                String columnName = rs.getString("column_name");
                String dataType = rs.getString("data_type");
                table.getColumns().add(SchemaColumnVO.builder()
                        .ordinalPosition(rs.getInt("ordinal_position"))
                        .columnName(columnName)
                        .fieldName(toCamelCase(columnName))
                        .dataType(dataType)
                        .columnType(rs.getString("column_type"))
                        .javaType(resolveJavaType(dataType))
                        .nullable("YES".equalsIgnoreCase(rs.getString("is_nullable")))
                        .primaryKey("PRI".equalsIgnoreCase(rs.getString("column_key")))
                        .defaultValue(rs.getString("column_default"))
                        .extra(rs.getString("extra"))
                        .columnComment(rs.getString("column_comment"))
                        .build());
            }
            return null;
        }, params.toArray());
    }

    private void appendTableNameFilter(StringBuilder sql, List<Object> params, List<String> tableNames) {
        if (CollectionUtils.isEmpty(tableNames)) {
            return;
        }

        sql.append(" and table_name in (");
        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
            params.add(tableNames.get(i));
        }
        sql.append(")");
    }

    private List<String> normalizeTableNames(List<String> tableNames) {
        if (CollectionUtils.isEmpty(tableNames)) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String tableName : tableNames) {
            if (StringUtils.hasText(tableName)) {
                result.add(tableName.trim());
            }
        }
        return result;
    }

    private String toCamelCase(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }

        StringBuilder builder = new StringBuilder();
        boolean upperNext = false;
        for (char ch : text.toCharArray()) {
            if (ch == '_') {
                upperNext = true;
                continue;
            }

            if (upperNext) {
                builder.append(Character.toUpperCase(ch));
                upperNext = false;
            } else {
                builder.append(Character.toLowerCase(ch));
            }
        }
        return builder.toString();
    }

    private String resolveJavaType(String dataType) {
        if (!StringUtils.hasText(dataType)) {
            return String.class.getSimpleName();
        }

        return switch (dataType.toLowerCase(Locale.ROOT)) {
            case "bigint" -> Long.class.getSimpleName();
            case "int", "integer", "mediumint", "smallint" -> Integer.class.getSimpleName();
            case "tinyint" -> Integer.class.getSimpleName();
            case "decimal", "numeric" -> BigDecimal.class.getSimpleName();
            case "double" -> Double.class.getSimpleName();
            case "float" -> Float.class.getSimpleName();
            case "datetime", "timestamp" -> LocalDateTime.class.getSimpleName();
            case "date" -> LocalDate.class.getSimpleName();
            case "time" -> LocalTime.class.getSimpleName();
            case "bit", "boolean" -> Boolean.class.getSimpleName();
            case "blob", "longblob", "mediumblob", "tinyblob", "binary", "varbinary" -> "byte[]";
            default -> String.class.getSimpleName();
        };
    }
}
