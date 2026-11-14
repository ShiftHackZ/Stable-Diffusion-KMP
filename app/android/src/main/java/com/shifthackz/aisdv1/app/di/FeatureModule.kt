package com.shifthackz.aisdv1.app.di

import com.shifthackz.aisdv1.feature.auth.di.authModule
import com.shifthackz.aisdv1.feature.openaioauth.di.openAiOAuthModule
import com.shifthackz.aisdv1.feature.benchmark.di.benchmarkModule
import com.shifthackz.aisdv1.feature.bonsai.di.bonsaiModule
import com.shifthackz.aisdv1.feature.coreml.di.coreMlModule
import com.shifthackz.aisdv1.feature.mediapipe.di.mediaPipeModule
import com.shifthackz.aisdv1.feature.onnx.di.onnxModule
import com.shifthackz.aisdv1.feature.sdxl.di.sdxlModule

val featureModule = arrayOf(
    authModule,
    openAiOAuthModule,
    benchmarkModule,
    bonsaiModule,
    coreMlModule,
    onnxModule,
    mediaPipeModule,
    sdxlModule,
)
