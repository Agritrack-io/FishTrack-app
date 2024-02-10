package io.agritrack.kefalonia.common.utilities;

import android.text.TextUtils;

import io.agritrack.kefalonia.data.dto.DbConfigDTO;

public class ConnectionStringUtils {

    public static String createDbConnectionString(DbConfigDTO configDTO) {

        if (configDTO.driver == null || configDTO.host == null || configDTO.port == null ||
                configDTO.dbase == null || configDTO.login == null || configDTO.pwd == null) {
            return null;
        }

        StringBuilder connectionStringBuilder = new StringBuilder();

        connectionStringBuilder.append(configDTO.driver.trim()).append("://");
        connectionStringBuilder.append(configDTO.host.trim());
        connectionStringBuilder.append(":").append(configDTO.port).append("/");
        connectionStringBuilder.append(configDTO.dbase.trim());
        if (!configDTO.login.isEmpty()) {
            connectionStringBuilder.append(";").append("user=").append(configDTO.login.trim());
        }
        if (!configDTO.pwd.isEmpty()) {
            connectionStringBuilder.append(";").append("password=").append(configDTO.pwd.trim());
        }

        return connectionStringBuilder.toString();
    }

    public static DbConfigDTO tokenizeDbConnectionString(String connectionString) throws Exception {

        if (TextUtils.isEmpty(connectionString))
            return new DbConfigDTO();

        DbConfigDTO configDTO = new DbConfigDTO();

        String subString = connectionString;

        String[] split = subString.split("://", 2);
        if (split.length == 1) {
            subString = split[0];
        } else {
            configDTO.driver = split[0];
            subString = split[1];
        }

        split = subString.split("/", 2);
        if (split.length == 1) {
            subString = split[0];
        } else {
            String[] ipWithPort = split[0].split(":", 2);
            configDTO.host = ipWithPort[0];
            if (ipWithPort.length > 1) {
                try {
                    configDTO.port = Long.parseLong(ipWithPort[1]);
                } catch (Exception e) {
                }
            }
            subString = split[1];
        }

        split = subString.split(";", 2);
        if (split.length == 1) {
            subString = split[0];
        } else {
            configDTO.dbase = split[0];
            subString = split[1];
        }

        split = subString.split(";", 2);
        configDTO.login = split[0].replace("user=", "");
        if (split.length > 1) {
            configDTO.pwd = split[1].replace("password=", "").replace(";", "").trim();
        }

        return configDTO;
    }
}
