package ru.plumsoftware.finance.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.plumsoftware.finance.presentation.onboarding.OnboardingViewModel

val presentationModule = module {
    viewModel { OnboardingViewModel(get()) }
}
