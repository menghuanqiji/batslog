package cn.com.pism.batslog.settings;

import cn.com.pism.batslog.constants.BatsLogConstant;
import cn.com.pism.batslog.converter.ColorConverter;
import cn.com.pism.batslog.converter.ConsoleColorConfigConverter;
import cn.com.pism.batslog.converter.DbTypeConverter;
import cn.com.pism.batslog.enums.DbType;
import cn.com.pism.batslog.model.ConsoleColorConfig;
import cn.com.pism.batslog.model.RgbColor;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author PerccyKing
 * @version 0.0.1
 * @date 2021/01/06 下午 08:47
 * @since 0.0.1
 */
@Data
@State(name = "BatsLogSettingState", storages = {
        @Storage(value = "BatsLog.xml", roamingType = RoamingType.DISABLED)
})
public class BatsLogSettingState implements PersistentStateComponent<BatsLogSettingState>, Serializable {


    /**
     * 日志SQL行截取前缀
     */
    private String sqlPrefix = BatsLogConstant.SQL_PREFIX;

    /**
     * 日志参数行前缀
     */
    private String paramsPrefix = BatsLogConstant.PARAMS_PREFIX;

    /**
     * 脱敏
     */
    private Boolean desensitize = Boolean.FALSE;

    /**
     * 美化
     */
    private Boolean prettyFormat = Boolean.TRUE;

    /**
     * 参数化
     */
    private Boolean parameterized = Boolean.FALSE;

    /**
     * 关键字转大写
     */
    private Boolean toUpperCase = Boolean.FALSE;

    /**
     * 数据库类型
     */
    @OptionTag(converter = DbTypeConverter.class)
    public DbType dbType = DbType.MYSQL;

    @OptionTag(converter = ColorConverter.class)
    private RgbColor keyWordDefCol = new RgbColor(204, 120, 50);

    @OptionTag(converter = ConsoleColorConfigConverter.class)
    private List<ConsoleColorConfig> colorConfigs = new ArrayList<>();


    @Nullable
    @Override
    public BatsLogSettingState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull BatsLogSettingState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
