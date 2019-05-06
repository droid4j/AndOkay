package com.dapn.andokay.logprint.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndokayLogPrintEntry implements Plugin<Project> {

    @Override
    void apply(Project project) {
        AppExtension appExtension = project.extensions.findByType(AppExtension.class)
        appExtension.registerTransform(new AndokayLogPrintTransform())
    }
}