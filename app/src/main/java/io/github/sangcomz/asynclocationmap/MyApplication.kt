package io.github.sangcomz.asynclocationmap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application Class
 *
 * Hilt를 사용하기 위한 Application 클래스입니다.
 * @HiltAndroidApp 어노테이션을 사용하여 Hilt의 코드 생성을 트리거합니다.
 *
 * 이 클래스는 앱의 모든 컴포넌트에서 의존성 주입을 사용할 수 있도록 합니다.
 * AndroidManifest.xml에서 이 클래스를 application의 name으로 등록해야 합니다.
 */
@HiltAndroidApp
class MyApplication : Application()
