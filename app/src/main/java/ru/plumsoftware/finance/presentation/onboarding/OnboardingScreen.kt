package ru.plumsoftware.finance.presentation.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.onboarding.components.OnboardingControlChartIllustration
import ru.plumsoftware.finance.presentation.onboarding.components.OnboardingIllustrationWithMascot
import ru.plumsoftware.finance.presentation.onboarding.components.OnboardingIncomeExpenseIllustration
import ru.plumsoftware.finance.presentation.onboarding.components.OnboardingMascot
import ru.plumsoftware.finance.presentation.onboarding.components.OnboardingPageIndicator
import ru.plumsoftware.finance.presentation.onboarding.components.OnboardingSmartSavingsIllustration
import ru.plumsoftware.finance.presentation.onboarding.components.OnboardingWelcomeIllustration
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue

private val onboardingPages = listOf(
    OnboardingPage(
        titleRes = R.string.onboarding_page1_title,
        subtitleRes = R.string.onboarding_page1_subtitle,
        mascotRes = R.drawable.onboarding_mascot_control,
        illustrationType = OnboardingIllustrationType.CONTROL_CHART,
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page2_title,
        subtitleRes = R.string.onboarding_page2_subtitle,
        mascotRes = R.drawable.onboarding_mascot_track,
        illustrationType = OnboardingIllustrationType.INCOME_EXPENSE,
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page3_title,
        subtitleRes = R.string.onboarding_page3_subtitle,
        mascotRes = R.drawable.onboarding_mascot_smart,
        illustrationType = OnboardingIllustrationType.SMART_SAVINGS,
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page4_title,
        subtitleRes = R.string.onboarding_page4_subtitle,
        mascotRes = R.drawable.onboarding_mascot_start,
        illustrationType = OnboardingIllustrationType.WELCOME,
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    val isLastPage = uiState.currentPage == onboardingPages.lastIndex

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Dimens.paddingSmall),
            ) {
                TextButton(
                    onClick = { viewModel.completeOnboarding() },
                    modifier = Modifier.align(Alignment.CenterEnd),
                    enabled = !uiState.isCompleting,
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        color = IosBlue,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = Dimens.paddingLarge),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                beyondViewportPageCount = 1,
            ) { page ->
                val data = onboardingPages[page]
                OnboardingPageContent(
                    page = data,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
            ) {
                OnboardingPageIndicator(
                    pageCount = onboardingPages.size,
                    currentPage = uiState.currentPage,
                )

                IosPrimaryButton(
                    text = stringResource(
                        if (isLastPage) R.string.onboarding_start else R.string.onboarding_next,
                    ),
                    onClick = {
                        if (isLastPage) {
                            viewModel.completeOnboarding()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(uiState.currentPage + 1)
                            }
                        }
                    },
                    loading = uiState.isCompleting && isLastPage,
                )
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(top = Dimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
    ) {
        when (page.illustrationType) {
            OnboardingIllustrationType.CONTROL_CHART -> {
                OnboardingIllustrationWithMascot(
                    illustration = { OnboardingControlChartIllustration() },
                    mascotRes = page.mascotRes,
                    mascotAlignment = Alignment.BottomEnd,
                )
            }
            OnboardingIllustrationType.INCOME_EXPENSE -> {
                OnboardingIllustrationWithMascot(
                    illustration = { OnboardingIncomeExpenseIllustration() },
                    mascotRes = page.mascotRes,
                    mascotAlignment = Alignment.BottomStart,
                )
            }
            OnboardingIllustrationType.SMART_SAVINGS -> {
                OnboardingIllustrationWithMascot(
                    illustration = { OnboardingSmartSavingsIllustration() },
                    mascotRes = page.mascotRes,
                    mascotAlignment = Alignment.BottomEnd,
                )
            }
            OnboardingIllustrationType.WELCOME -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OnboardingWelcomeIllustration()
                    OnboardingMascot(
                        mascotRes = page.mascotRes,
                        sizeDp = Dimens.mascotOnboarding,
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
        ) {
            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(page.subtitleRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                textAlign = TextAlign.Start,
            )
        }
    }
}
