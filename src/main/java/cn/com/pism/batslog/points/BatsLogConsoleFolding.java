package cn.com.pism.batslog.points;

import cn.com.pism.batslog.settings.BatsLogSettingState;
import cn.com.pism.batslog.util.BatsLogUtil;
import cn.com.pism.batslog.util.SqlFormatUtil;
import com.intellij.execution.ConsoleFolding;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static cn.com.pism.batslog.constants.BatsLogConstant.PARAMS_PREFIX;
import static cn.com.pism.batslog.constants.BatsLogConstant.SQL_PREFIX;
import static cn.com.pism.batslog.util.BatsLogUtil.SOURCE_SQL_LIST_MAP;

/**
 * @author PerccyKing
 * @version 0.0.1
 * @date 2020/10/24 下午 09:06
 * @since 0.0.1
 */
public class BatsLogConsoleFolding extends ConsoleFolding {

    public static final Integer LIST_SIZE = 2;

    /**
     * <pre>
     * line = line.replace("\n", "");
     *         List<String> sourceSqlList = SOURCE_SQL_LIST_MAP.get(project);
     *         if ((CollectionUtils.isEmpty(sourceSqlList)) || (sourceSqlList.size() > LIST_SIZE)) {
     *             sourceSqlList = new ArrayList<>();
     *         }
     *         //sql和参数占用多行
     *         if (BatsLogUtil.getTailStatus(project)) {
     *             if (line.contains(SQL_PREFIX) && sourceSqlList.size() == 0) {
     *                 sourceSqlList.add(line);
     *                 SOURCE_SQL_LIST_MAP.put(project, sourceSqlList);
     *             } else if (sourceSqlList.size() >= LIST_SIZE) {
     *                 SOURCE_SQL_LIST_MAP.remove(project);
     *             }
     *
     *             if (line.contains(PARAMS_PREFIX) && sourceSqlList.size() != 0) {
     *                 sourceSqlList.add(line);
     *                 SOURCE_SQL_LIST_MAP.put(project, sourceSqlList);
     *                 if (sourceSqlList.size() == LIST_SIZE) {
     *                     SqlFormatUtil.format(String.join("\n", sourceSqlList), project);
     *                     SOURCE_SQL_LIST_MAP.remove(project);
     *                 }
     *             }
     *         }
     *         </pre>
     *
     * @param project current project
     * @param line    line to check whether it should be folded or not
     * @return {@code true} if line should be folded, {@code false} if not
     */
    @Override
    public boolean shouldFoldLine(@NotNull Project project, @NotNull String line) {
        //先判断有没有开启SQL监听
        if (BatsLogUtil.getTailStatus(project)) {

            BatsLogSettingState service = ServiceManager.getService(project, BatsLogSettingState.class);

            String sqlPrefix = StringUtils.isBlank(service.getSqlPrefix()) ? SQL_PREFIX : service.getSqlPrefix();
            String paramsPrefix = StringUtils.isBlank(service.getParamsPrefix()) ? PARAMS_PREFIX : service.getParamsPrefix();

            //从缓存中获取一次日志
            List<String> sourceSqlList = SOURCE_SQL_LIST_MAP.get(project);
            if ((CollectionUtils.isEmpty(sourceSqlList))) {
                //缓存中部存在新增一个列表
                sourceSqlList = new ArrayList<>();
                //缓存为空，当前行如果匹配SQL行直接放入缓存
                if (line.contains(sqlPrefix)) {
                    sourceSqlList.add(line);
                    SOURCE_SQL_LIST_MAP.put(project, sourceSqlList);
                }
            } else {
                //缓存不为空，判断缓存中 最后一行日志是否以 `)`结束
                String lastLine = sourceSqlList.get(sourceSqlList.size() - 1);
                boolean maybeEndLine = lastLine.replace("\n", "").endsWith(")");
                if (maybeEndLine) {
                    maybeEndLine = !SqlFormatUtil.nextLineIsParams(lastLine);
                }
                if (maybeEndLine) {
                    //缓存末行正常结束，判断日志中 参数和SQL存在数量，数量一致，进行格式化
                    String logs = String.join("\n", sourceSqlList);
                    if (StringUtils.countMatches(logs, sqlPrefix) != 0 &&
                            StringUtils.countMatches(logs, paramsPrefix) != 0 &&
                            (StringUtils.countMatches(logs, sqlPrefix) == StringUtils.countMatches(logs, paramsPrefix))) {
                        SOURCE_SQL_LIST_MAP.remove(project);
                        sourceSqlList = new ArrayList<>();
                        SqlFormatUtil.format(logs, project);
                    }
                }
                sourceSqlList.add(line);
                SOURCE_SQL_LIST_MAP.put(project, sourceSqlList);
            }
        }
        return super.shouldFoldLine(project, line);
    }
}
