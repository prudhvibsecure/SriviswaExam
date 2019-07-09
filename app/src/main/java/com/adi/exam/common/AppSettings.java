package com.adi.exam.common;

import com.adi.exam.utils.TraceUtils;

import java.util.Properties;

public class AppSettings {

    private static AppSettings settings = null;

    private Properties properties = null;


    public static AppSettings getInstance() {

        if (settings == null)
            settings = new AppSettings();

        return settings;

    }

    private AppSettings() {
        loadProperties();
    }

    private void loadProperties() {

        try {

            properties = new Properties();

            properties.setProperty("studentlogin", "https://bsecuresoftechsolutions.com/viswa/student_login");

            properties.setProperty("checkqustionpaper", "https://bsecuresoftechsolutions.com/viswa/admin/check_question_paper");

            properties.setProperty("getquestionpaper", "https://bsecuresoftechsolutions.com/viswa/admin/get_question_paper");

            properties.setProperty("updatedevice", "https://bsecuresoftechsolutions.com/viswa/admin/update_device");

            properties.setProperty("checkversion", "https://bsecuresoftechsolutions.com/viswa/admin/check_version");

            properties.setProperty("getmaterial", "https://bsecuresoftechsolutions.com/viswa/material/get_material");

            properties.setProperty("checkassignment", "https://bsecuresoftechsolutions.com/viswa/assignment/check_assignment");

            properties.setProperty("getassignment", "https://bsecuresoftechsolutions.com/viswa/assignment/get_assignment");

            properties.setProperty("uploadfile_admin", "https://bsecuresoftechsolutions.com/viswa/admin/uploadfile");

            properties.setProperty("submit_exam_result", "https://bsecuresoftechsolutions.com/viswa/admin/submit_exam_result");

            properties.setProperty("submit_assignment_result", "https://bsecuresoftechsolutions.com/viswa/admin/submit_assignment_result");

            properties.setProperty("get_student_details", "https://bsecuresoftechsolutions.com/viswa/get_student_details");

            properties.setProperty("update_password", "https://bsecuresoftechsolutions.com/viswa/update_password");



        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    public String getPropertyValue(String key) {

        return properties.getProperty(key, "");

    }

}
