package com.admin.util;

import com.admin.entity.GenTable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 代码生成工具 — 模板渲染
 */
public class GenUtils {

    public static String toCamelCase(String name, boolean upperFirst) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = upperFirst;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
                continue;
            }
            sb.append(nextUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
            nextUpper = false;
        }
        return sb.toString();
    }

    public static String toKebabCase(String name) {
        return name.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }

    /** 获取主键列 */
    private static Map<String, Object> getPkColumn(List<Map<String, Object>> columns) {
        for (Map<String, Object> col : columns) {
            if ("PRI".equals(col.get("column_key"))) return col;
        }
        return columns.isEmpty() ? null : columns.get(0);
    }

    /** 字段转 Java 类型 */
    private static String dbTypeToJava(String dbType) {
        if (dbType == null) return "String";
        String t = dbType.toLowerCase();
        if (t.contains("bigint")) return "Long";
        if (t.contains("int") || t.contains("tinyint")) return "Integer";
        if (t.contains("double") || t.contains("decimal") || t.contains("float")) return "Double";
        if (t.contains("datetime") || t.contains("timestamp")) return "LocalDateTime";
        if (t.contains("date")) return "LocalDate";
        if (t.contains("boolean")) return "Boolean";
        return "String";
    }

    private static String colToJavaName(String colName) {
        return toCamelCase(colName, false);
    }

    // ======================= Java 代码模板 =======================

    public static String renderEntity(GenTable t, List<Map<String, Object>> cols) {
        String pkg = t.getPackageName();
        StringBuilder fields = new StringBuilder();
        for (Map<String, Object> c : cols) {
            String col = String.valueOf(c.get("column_name"));
            String javaType = dbTypeToJava(String.valueOf(c.get("data_type")));
            String comment = String.valueOf(c.getOrDefault("column_comment", ""));
            String javaName = colToJavaName(col);
            boolean isPk = "PRI".equals(c.get("column_key"));

            if (isPk) fields.append("    @TableId(type = IdType.AUTO)\n");
            if ("createTime".equals(javaName))
                fields.append("    @TableField(fill = FieldFill.INSERT)\n");
            if ("updateTime".equals(javaName))
                fields.append("    @TableField(fill = FieldFill.INSERT_UPDATE)\n");

            fields.append("    /** ").append(comment).append(" */\n");
            fields.append("    private ").append(javaType).append(" ").append(javaName).append(";\n\n");
        }

        return "package " + pkg + ".entity;\n\n" +
                "import com.baomidou.mybatisplus.annotation.*;\n" +
                "import lombok.Data;\n" +
                "import java.time.*;\n\n" +
                "@Data\n" +
                "@TableName(\"" + t.getTableName() + "\")\n" +
                "public class " + t.getClassName() + " {\n\n" +
                fields +
                "}\n";
    }

    public static String renderMapper(GenTable t, List<Map<String, Object>> cols) {
        String pkg = t.getPackageName();
        return "package " + pkg + ".mapper;\n\n" +
                "import " + pkg + ".entity." + t.getClassName() + ";\n" +
                "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n\n" +
                "public interface " + t.getClassName() + "Mapper extends BaseMapper<" + t.getClassName() + "> {\n" +
                "}\n";
    }

    public static String renderService(GenTable t, List<Map<String, Object>> cols) {
        String pkg = t.getPackageName();
        return "package " + pkg + ".service;\n\n" +
                "import " + pkg + ".entity." + t.getClassName() + ";\n" +
                "import com.baomidou.mybatisplus.extension.service.IService;\n\n" +
                "public interface " + t.getClassName() + "Service extends IService<" + t.getClassName() + "> {\n" +
                "}\n";
    }

    public static String renderServiceImpl(GenTable t, List<Map<String, Object>> cols) {
        String pkg = t.getPackageName();
        return "package " + pkg + ".service.impl;\n\n" +
                "import " + pkg + ".entity." + t.getClassName() + ";\n" +
                "import " + pkg + ".mapper." + t.getClassName() + "Mapper;\n" +
                "import " + pkg + ".service." + t.getClassName() + "Service;\n" +
                "import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n" +
                "import org.springframework.stereotype.Service;\n\n" +
                "@Service\n" +
                "public class " + t.getClassName() + "ServiceImpl extends ServiceImpl<" +
                t.getClassName() + "Mapper, " + t.getClassName() + "> implements " + t.getClassName() + "Service {\n" +
                "}\n";
    }

    public static String renderController(GenTable t, List<Map<String, Object>> cols) {
        String pkg = t.getPackageName();
        String businessName = t.getBusinessName();
        String reqPath = "/" + businessName.replaceAll("([A-Z])", "-$1").toLowerCase().replaceFirst("^-", "");
        String permPrefix = reqPath.replace("-", ":");
        String entityVar = toCamelCase(t.getClassName(), false);

        return "package " + pkg + ".controller;\n\n" +
                "import " + pkg + ".common.annotation.Log;\n" +
                "import " + pkg + ".common.enums.BusinessType;\n" +
                "import " + pkg + ".common.result.Result;\n" +
                "import " + pkg + ".entity." + t.getClassName() + ";\n" +
                "import " + pkg + ".service." + t.getClassName() + "Service;\n" +
                "import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;\n" +
                "import com.baomidou.mybatisplus.core.metadata.IPage;\n" +
                "import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n" +
                "import lombok.RequiredArgsConstructor;\n" +
                "import org.springframework.security.access.prepost.PreAuthorize;\n" +
                "import org.springframework.web.bind.annotation.*;\n\n" +
                "@RestController\n" +
                "@RequestMapping(\"" + reqPath + "\")\n" +
                "@RequiredArgsConstructor\n" +
                "public class " + t.getClassName() + "Controller {\n\n" +
                "    private final " + t.getClassName() + "Service " + entityVar + "Service;\n\n" +
                "    @GetMapping(\"/page\")\n" +
                "    @PreAuthorize(\"hasAuthority('" + permPrefix + ":list')\")\n" +
                "    public Result<IPage<" + t.getClassName() + ">> page(Long pageNum, Long pageSize) {\n" +
                "        Page<" + t.getClassName() + "> page = new Page<>(pageNum, pageSize);\n" +
                "        LambdaQueryWrapper<" + t.getClassName() + "> wrapper = new LambdaQueryWrapper<>();\n" +
                "        wrapper.orderByDesc(" + t.getClassName() + "::getCreateTime);\n" +
                "        return Result.success(" + entityVar + "Service.page(page, wrapper));\n" +
                "    }\n\n" +
                "    @GetMapping(\"/{id}\")\n" +
                "    public Result<" + t.getClassName() + "> getInfo(@PathVariable Long id) {\n" +
                "        return Result.success(" + entityVar + "Service.getById(id));\n" +
                "    }\n\n" +
                "    @PostMapping\n" +
                "    @Log(title = \"" + t.getFunctionName() + "\", businessType = BusinessType.INSERT)\n" +
                "    @PreAuthorize(\"hasAuthority('" + permPrefix + ":add')\")\n" +
                "    public Result<Void> add(@RequestBody " + t.getClassName() + " entity) {\n" +
                "        " + entityVar + "Service.save(entity);\n" +
                "        return Result.success();\n" +
                "    }\n\n" +
                "    @PutMapping\n" +
                "    @Log(title = \"" + t.getFunctionName() + "\", businessType = BusinessType.UPDATE)\n" +
                "    @PreAuthorize(\"hasAuthority('" + permPrefix + ":edit')\")\n" +
                "    public Result<Void> update(@RequestBody " + t.getClassName() + " entity) {\n" +
                "        " + entityVar + "Service.updateById(entity);\n" +
                "        return Result.success();\n" +
                "    }\n\n" +
                "    @DeleteMapping(\"/{id}\")\n" +
                "    @Log(title = \"" + t.getFunctionName() + "\", businessType = BusinessType.DELETE)\n" +
                "    @PreAuthorize(\"hasAuthority('" + permPrefix + ":remove')\")\n" +
                "    public Result<Void> remove(@PathVariable Long id) {\n" +
                "        " + entityVar + "Service.removeById(id);\n" +
                "        return Result.success();\n" +
                "    }\n" +
                "}\n";
    }

    public static String renderVue(GenTable t, List<Map<String, Object>> cols) {
        List<Map<String, Object>> listCols = cols.stream()
                .filter(c -> !"create_time".equalsIgnoreCase(String.valueOf(c.get("column_name")))
                        && !"update_time".equalsIgnoreCase(String.valueOf(c.get("column_name"))))
                .limit(6).collect(Collectors.toList());

        StringBuilder tableCols = new StringBuilder();
        for (Map<String, Object> c : listCols) {
            String col = String.valueOf(c.get("column_name"));
            String comment = String.valueOf(c.getOrDefault("column_comment", col));
            String javaName = colToJavaName(col);
            if ("id".equals(javaName)) continue;
            tableCols.append("        <el-table-column label=\"").append(comment)
                    .append("\" prop=\"").append(javaName).append("\" min-width=\"120\" />\n");
        }

        StringBuilder formItems = new StringBuilder();
        for (Map<String, Object> c : cols) {
            String col = String.valueOf(c.get("column_name"));
            String comment = String.valueOf(c.getOrDefault("column_comment", col));
            String javaName = colToJavaName(col);
            if ("id".equals(javaName) || "createTime".equals(javaName) || "updateTime".equals(javaName)) continue;
            formItems.append("        <el-form-item label=\"").append(comment)
                    .append("\" prop=\"").append(javaName).append("\">\n")
                    .append("          <el-input v-model=\"form.").append(javaName)
                    .append("\" placeholder=\"请输入").append(comment).append("\" />\n")
                    .append("        </el-form-item>\n");
        }

        String reqPath = "/" + t.getBusinessName().replaceAll("([A-Z])", "-$1").toLowerCase().replaceFirst("^-", "");

        return "<template>\n" +
                "  <div class=\"page-container\">\n" +
                "    <el-card shadow=\"never\" style=\"margin-top: 16px;\">\n" +
                "      <div class=\"toolbar\">\n" +
                "        <el-button type=\"primary\" :icon=\"Plus\" @click=\"openDialog\">新增</el-button>\n" +
                "      </div>\n" +
                "      <el-table :data=\"tableData\" border stripe v-loading=\"loading\">\n" +
                "        <el-table-column label=\"序号\" type=\"index\" width=\"60\" align=\"center\" />\n" +
                tableCols +
                "        <el-table-column label=\"操作\" width=\"150\" align=\"center\" fixed=\"right\">\n" +
                "          <template #default=\"{ row }\">\n" +
                "            <el-button type=\"primary\" link size=\"small\" :icon=\"Edit\" @click=\"edit(row)\">编辑</el-button>\n" +
                "            <el-button type=\"danger\" link size=\"small\" :icon=\"Delete\" @click=\"del(row)\">删除</el-button>\n" +
                "          </template>\n" +
                "        </el-table-column>\n" +
                "      </el-table>\n" +
                "      <div class=\"pagination-container\">\n" +
                "        <el-pagination v-model:current-page=\"pageNum\" v-model:page-size=\"pageSize\"\n" +
                "          :total=\"total\" :page-sizes=\"[10,20,50,100]\"\n" +
                "          layout=\"total,sizes,prev,pager,next,jumper\" background @change=\"loadData\" />\n" +
                "      </div>\n" +
                "    </el-card>\n\n" +
                "    <el-dialog v-model=\"dialogVisible\" :title=\"form.id ? '编辑' : '新增'\" width=\"520px\" destroy-on-close>\n" +
                "      <el-form ref=\"formRef\" :model=\"form\" label-width=\"100px\">\n" +
                formItems +
                "      </el-form>\n" +
                "      <template #footer>\n" +
                "        <el-button @click=\"dialogVisible = false\">取 消</el-button>\n" +
                "        <el-button type=\"primary\" @click=\"save\" :loading=\"saving\">确 定</el-button>\n" +
                "      </template>\n" +
                "    </el-dialog>\n" +
                "  </div>\n" +
                "</template>\n\n" +
                "<script setup>\n" +
                "import { ref, onMounted } from 'vue'\n" +
                "import request from '@/utils/request'\n" +
                "import { ElMessage, ElMessageBox } from 'element-plus'\n" +
                "import { Plus, Edit, Delete } from '@element-plus/icons-vue'\n\n" +
                "const tableData = ref([])\n" +
                "const total = ref(0)\n" +
                "const pageNum = ref(1)\n" +
                "const pageSize = ref(10)\n" +
                "const loading = ref(false)\n" +
                "const saving = ref(false)\n" +
                "const dialogVisible = ref(false)\n" +
                "const formRef = ref(null)\n" +
                "const form = ref({})\n\n" +
                "const loadData = async () => {\n" +
                "  loading.value = true\n" +
                "  try {\n" +
                "    const res = await request.get('" + reqPath + "/page', { params: { pageNum: pageNum.value, pageSize: pageSize.value } })\n" +
                "    tableData.value = res.data.records\n" +
                "    total.value = res.data.total\n" +
                "  } finally { loading.value = false }\n" +
                "}\n\n" +
                "const openDialog = () => { form.value = {}; dialogVisible.value = true }\n" +
                "const edit = async (row) => {\n" +
                "  const res = await request.get(`" + reqPath + "/${row.id}`)\n" +
                "  form.value = res.data\n" +
                "  dialogVisible.value = true\n" +
                "}\n" +
                "const save = async () => {\n" +
                "  saving.value = true\n" +
                "  try {\n" +
                "    if (form.value.id) { await request.put('" + reqPath + "', form.value) }\n" +
                "    else { await request.post('" + reqPath + "', form.value) }\n" +
                "    dialogVisible.value = false\n" +
                "    ElMessage.success('保存成功')\n" +
                "    loadData()\n" +
                "  } finally { saving.value = false }\n" +
                "}\n" +
                "const del = async (row) => {\n" +
                "  await ElMessageBox.confirm('确认删除吗？', '提示', { type: 'warning' })\n" +
                "  await request.delete(`" + reqPath + "/${row.id}`)\n" +
                "  ElMessage.success('删除成功')\n" +
                "  loadData()\n" +
                "}\n" +
                "onMounted(loadData)\n" +
                "</script>\n\n" +
                "<style scoped>\n.page-container { height: 100%; }\n</style>\n";
    }

    public static String renderSql(GenTable t, List<Map<String, Object>> cols) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- ").append(t.getTableName()).append(" 表初始化数据\n\n");
        sb.append("DROP TABLE IF EXISTS `").append(t.getTableName()).append("`;\n");
        sb.append("CREATE TABLE `").append(t.getTableName()).append("` (\n");

        for (int i = 0; i < cols.size(); i++) {
            Map<String, Object> c = cols.get(i);
            String col = String.valueOf(c.get("column_name"));
            String type = String.valueOf(c.get("data_type"));
            String comment = String.valueOf(c.getOrDefault("column_comment", ""));
            String nullable = "YES".equals(c.get("is_nullable")) ? "NULL" : "NOT NULL";
            String pk = "PRI".equals(c.get("column_key")) ? " AUTO_INCREMENT" : "";
            String comma = i < cols.size() - 1 ? "," : "";

            sb.append("  `").append(col).append("` ").append(type.toUpperCase());
            if (pk.isEmpty()) sb.append(" ").append(nullable);
            sb.append(pk);
            if (!comment.isEmpty()) sb.append(" COMMENT '").append(comment).append("'");
            sb.append(comma).append("\n");
        }
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        return sb.toString();
    }
}
