package io.github.sangcomz.asynclocationmap

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application Class
 *
 * Hilt를 사용하기 위한 Application 클래스입니다.
 * @HiltAndroidApp 어노테이션을 사용하여 Hilt의 코드 생성을 트리거합니다.
 *
 * WorkManager를 수동으로 초기화하여 HiltWorkerFactory를 사용하도록 설정합니다.
 * 이를 통해 @HiltWorker 어노테이션이 붙은 Worker에서 의존성 주입을 사용할 수 있습니다.
 *
 * 이 클래스는 앱의 모든 컴포넌트에서 의존성 주입을 사용할 수 있도록 합니다.
 * AndroidManifest.xml에서 이 클래스를 application의 name으로 등록해야 합니다.
 *
 * 주의사항:
 * - AndroidManifest.xml에서 WorkManagerInitializer를 비활성화해야 합니다
 * - onCreate()에서 WorkManager.initialize()를 명시적으로 호출해야 합니다
 */
@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    /**
     * HiltWorkerFactory
     *
     * Hilt가 Worker에 의존성을 주입할 수 있도록 하는 Factory입니다.
     * @Inject를 통해 Hilt가 자동으로 주입합니다.
     */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
