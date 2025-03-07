/*
 * Copyright (C) 2019 Sun.Hao(https://www.crazy-coder.cn/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lodsve.core.configuration;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 日志文件配置.
 *
 * @author <a href="mailto:sunhao.java@gmail.com">sunhao(sunhao.java@gmail.com)</a>
 */
@Setter
@Getter
public class LogbackConfig {
    /**
     * logback配置文件
     */
    private String config;
    /**
     * logback日志文件路径
     */
    private String logFile;
    /**
     * 控制台打印格式化字符串
     */
    private String consoleLogPattern;
    /**
     * 打印到日志文件的格式化字符串
     */
    private String fileLogPattern;
    /**
     * 打印到日志文件的最大文件大小
     */
    private String fileLogMaxSize;
    /**
     * 打印到日志文件的最大个数
     */
    private Integer fileLogMaxHistory;
    /**
     * 日志级别配置 
     */
    private Map<String, String> level = Maps.newHashMap();
}
