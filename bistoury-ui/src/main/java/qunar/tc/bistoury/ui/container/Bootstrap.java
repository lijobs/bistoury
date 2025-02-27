/*
 * Copyright (C) 2019 Qunar, Inc.
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package qunar.tc.bistoury.ui.container;

import com.google.common.base.Strings;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.util.ServerInfo;
import org.apache.tomcat.util.res.StringManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.bistoury.serverside.configuration.DynamicConfig;
import qunar.tc.bistoury.serverside.configuration.DynamicConfigLoader;
import qunar.tc.bistoury.serverside.util.ServerManager;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * @author: leix.xie
 * @date: 2019/7/15 11:12
 * @describe：注意：该类仅用于脚本打包启动，本地调试请使用tomcat启动，在启动前，请先将pom文件中的打包方式修改为war
 */
public class Bootstrap {
    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    protected static final StringManager sm = StringManager.getManager(Constants.Package);

    public static void main(String[] args) {
        try {
            final String confDir = System.getProperty("bistoury.conf");
            if (Strings.isNullOrEmpty(confDir)) {
                throw new RuntimeException("请在JVM参数中配置项目配置文件目录，即bistoury.conf");
            }
            DynamicConfig config = DynamicConfigLoader.load("server.properties");

            int port = config.getInt("tomcat.port");
            System.setProperty("bistoury.tomcat.pory", String.valueOf(port));

            Tomcat tomcat = new Tomcat();
            tomcat.setPort(port);
            tomcat.setBaseDir(config.getString("tomcat.basedir"));
            tomcat.getHost().setAutoDeploy(false);

            String webappDirLocation = "../webapp/";
            StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());

            String contextPath = "";
            ctx.setPath(contextPath);
            ctx.addLifecycleListener(new Tomcat.FixContextListener());
            ctx.setName("bistoury-ui");
            tomcat.getHost().addChild(ctx);

            log(webappDirLocation, confDir);

            logger.info("Server配置加载完成，正在启动中...");
            tomcat.start();
            logger.info("Server启动成功");
            tomcat.getServer().await();
        } catch (Exception e) {
            logger.error("Server启动失败...", e);
        }
    }

    public static void log(final String webappDirLocation, final String confDir) {
        logger.info("Server webapp docBase: {}", new File("" + webappDirLocation).getAbsolutePath());
        logger.info("Server project dir:    {}", new File("").getAbsolutePath());
        logger.info("Server config dir:     {}", confDir);
        logger.info(sm.getString("versionLoggerListener.serverInfo.server.version",
                ServerInfo.getServerInfo()));
        logger.info(sm.getString("versionLoggerListener.serverInfo.server.built",
                ServerInfo.getServerBuilt()));
        logger.info(sm.getString("versionLoggerListener.serverInfo.server.number",
                ServerInfo.getServerNumber()));
        logger.info(sm.getString("versionLoggerListener.os.name",
                System.getProperty("os.name")));
        logger.info(sm.getString("versionLoggerListener.os.version",
                System.getProperty("os.version")));
        logger.info(sm.getString("versionLoggerListener.os.arch",
                System.getProperty("os.arch")));
        logger.info(sm.getString("versionLoggerListener.java.home",
                System.getProperty("java.home")));
        logger.info(sm.getString("versionLoggerListener.vm.version",
                System.getProperty("java.runtime.version")));
        logger.info(sm.getString("versionLoggerListener.vm.vendor",
                System.getProperty("java.vm.vendor")));
        logger.info(sm.getString("versionLoggerListener.catalina.base",
                System.getProperty("catalina.base")));
        logger.info(sm.getString("versionLoggerListener.catalina.home",
                System.getProperty("catalina.home")));

        //argument
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String arg : args) {
            logger.info(sm.getString("versionLoggerListener.arg", arg));
        }

        ServerManager.printServerConfig();
    }
}
