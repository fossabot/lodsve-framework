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

package lodsve.core.context;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * .
 *
 * @author <a href="mailto:sunhao.java@gmail.com">sunhao(sunhao.java@gmail.com)</a>
 * @date 2016/12/20 上午10:54
 */
public class SpringSupportTest extends SpringSupport {
    @Autowired
    private DemoService demoService;

    private void say() {
        demoService.say();
    }

    public static void main(String[] args) {
        SpringSupportTest test = new SpringSupportTest();
        test.say();

        DemoService demoService = ApplicationHelper.getInstance().getBean(DemoService.class);
        demoService.say();
    }

    @Override
    public String supportConfigLocation() {
        return "spring/application-context.xml";
    }
}
