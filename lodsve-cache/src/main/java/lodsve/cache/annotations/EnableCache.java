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

package lodsve.cache.annotations;

import lodsve.core.configuration.EnableLodsve;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用cache模块.
 *
 * @author <a href="mailto:sunhao.java@gmail.com">sunhao(sunhao.java@gmail.com)</a>
 * @date 2016/1/19 15:10
 * @see CacheImportSelector
 * @see CacheMode
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableLodsve
@Import({CacheImportSelector.class})
public @interface EnableCache {
    /**
     * 选择使用的缓存类型
     *
     * @return 缓存类型
     * @see CacheMode
     */
    CacheMode cache() default CacheMode.REDIS;
}
