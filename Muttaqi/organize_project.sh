#!/bin/bash

# 📁 Automatic Project Organization Script
# This script organizes your Muttaqi project into Clean Architecture folders

echo "🚀 Starting automatic project organization..."
echo ""

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR"

echo "📂 Project root: $PROJECT_ROOT"
echo ""

# Create folder structure
echo "📁 Creating folder structure..."

mkdir -p "$PROJECT_ROOT/App"
mkdir -p "$PROJECT_ROOT/Domain/Entities"
mkdir -p "$PROJECT_ROOT/Domain/ValueObjects"
mkdir -p "$PROJECT_ROOT/Domain/Errors"
mkdir -p "$PROJECT_ROOT/Data/Network"
mkdir -p "$PROJECT_ROOT/Data/Repositories/Implementations"
mkdir -p "$PROJECT_ROOT/Data/Preferences"
mkdir -p "$PROJECT_ROOT/Data/UseCases/Quran"
mkdir -p "$PROJECT_ROOT/Presentation/Onboarding/ViewModels"
mkdir -p "$PROJECT_ROOT/Presentation/Onboarding/Views"
mkdir -p "$PROJECT_ROOT/Presentation/Quran/QuranList/ViewModels"
mkdir -p "$PROJECT_ROOT/Presentation/Quran/QuranList/Views"
mkdir -p "$PROJECT_ROOT/Presentation/Quran/SurahDetail/ViewModels"
mkdir -p "$PROJECT_ROOT/Presentation/Quran/SurahDetail/Views"
mkdir -p "$PROJECT_ROOT/Core/DependencyInjection"
mkdir -p "$PROJECT_ROOT/Core/Navigation"
mkdir -p "$PROJECT_ROOT/Core/Services"
mkdir -p "$PROJECT_ROOT/Core/Utilities"
mkdir -p "$PROJECT_ROOT/Tests/PresentationTests"
mkdir -p "$PROJECT_ROOT/Documentation"

echo "✅ Folder structure created!"
echo ""

# Move files to appropriate locations
echo "📦 Moving files..."

# App
[ -f "$PROJECT_ROOT/MuttaqiApp.swift" ] && mv "$PROJECT_ROOT/MuttaqiApp.swift" "$PROJECT_ROOT/App/"

# Domain - Entities
[ -f "$PROJECT_ROOT/Surah.swift" ] && mv "$PROJECT_ROOT/Surah.swift" "$PROJECT_ROOT/Domain/Entities/"
[ -f "$PROJECT_ROOT/Ayah.swift" ] && mv "$PROJECT_ROOT/Ayah.swift" "$PROJECT_ROOT/Domain/Entities/"

# Domain - ValueObjects
[ -f "$PROJECT_ROOT/FontSize.swift" ] && mv "$PROJECT_ROOT/FontSize.swift" "$PROJECT_ROOT/Domain/ValueObjects/"
[ -f "$PROJECT_ROOT/Language.swift" ] && mv "$PROJECT_ROOT/Language.swift" "$PROJECT_ROOT/Domain/ValueObjects/"
[ -f "$PROJECT_ROOT/SurahNumber.swift" ] && mv "$PROJECT_ROOT/SurahNumber.swift" "$PROJECT_ROOT/Domain/ValueObjects/"
[ -f "$PROJECT_ROOT/ReadingMode.swift" ] && mv "$PROJECT_ROOT/ReadingMode.swift" "$PROJECT_ROOT/Domain/ValueObjects/"

# Domain - Errors
[ -f "$PROJECT_ROOT/SurahDetailError.swift" ] && mv "$PROJECT_ROOT/SurahDetailError.swift" "$PROJECT_ROOT/Domain/Errors/"

# Data - Network
[ -f "$PROJECT_ROOT/QuranAPIService.swift" ] && mv "$PROJECT_ROOT/QuranAPIService.swift" "$PROJECT_ROOT/Data/Network/"

# Data - Repositories
[ -f "$PROJECT_ROOT/QuranRepository.swift" ] && mv "$PROJECT_ROOT/QuranRepository.swift" "$PROJECT_ROOT/Data/Repositories/Implementations/"
[ -f "$PROJECT_ROOT/QuranSyncRepository.swift" ] && mv "$PROJECT_ROOT/QuranSyncRepository.swift" "$PROJECT_ROOT/Data/Repositories/Implementations/"
[ -f "$PROJECT_ROOT/ReadingProgressRepository.swift" ] && mv "$PROJECT_ROOT/ReadingProgressRepository.swift" "$PROJECT_ROOT/Data/Repositories/Implementations/"

# Data - Preferences
[ -f "$PROJECT_ROOT/ReadingPreferencesStore.swift" ] && mv "$PROJECT_ROOT/ReadingPreferencesStore.swift" "$PROJECT_ROOT/Data/Preferences/"

# Data - UseCases
[ -f "$PROJECT_ROOT/FetchSurahsUseCase.swift" ] && mv "$PROJECT_ROOT/FetchSurahsUseCase.swift" "$PROJECT_ROOT/Data/UseCases/Quran/"
[ -f "$PROJECT_ROOT/FetchAyahsUseCase.swift" ] && mv "$PROJECT_ROOT/FetchAyahsUseCase.swift" "$PROJECT_ROOT/Data/UseCases/Quran/"
[ -f "$PROJECT_ROOT/SyncQuranDataUseCase.swift" ] && mv "$PROJECT_ROOT/SyncQuranDataUseCase.swift" "$PROJECT_ROOT/Data/UseCases/Quran/"
[ -f "$PROJECT_ROOT/GetLastReadingUseCase.swift" ] && mv "$PROJECT_ROOT/GetLastReadingUseCase.swift" "$PROJECT_ROOT/Data/UseCases/Quran/"

# Presentation - Onboarding
[ -f "$PROJECT_ROOT/OnboardingViewModel.swift" ] && mv "$PROJECT_ROOT/OnboardingViewModel.swift" "$PROJECT_ROOT/Presentation/Onboarding/ViewModels/"
[ -f "$PROJECT_ROOT/OnboardingView.swift" ] && mv "$PROJECT_ROOT/OnboardingView.swift" "$PROJECT_ROOT/Presentation/Onboarding/Views/"
[ -f "$PROJECT_ROOT/SetupStepView.swift" ] && mv "$PROJECT_ROOT/SetupStepView.swift" "$PROJECT_ROOT/Presentation/Onboarding/Views/"
[ -f "$PROJECT_ROOT/LocationStepView.swift" ] && mv "$PROJECT_ROOT/LocationStepView.swift" "$PROJECT_ROOT/Presentation/Onboarding/Views/"

# Presentation - Quran List
[ -f "$PROJECT_ROOT/QuranListViewModel.swift" ] && mv "$PROJECT_ROOT/QuranListViewModel.swift" "$PROJECT_ROOT/Presentation/Quran/QuranList/ViewModels/"
[ -f "$PROJECT_ROOT/QuranListView.swift" ] && mv "$PROJECT_ROOT/QuranListView.swift" "$PROJECT_ROOT/Presentation/Quran/QuranList/Views/"
[ -f "$PROJECT_ROOT/SurahCardView.swift" ] && mv "$PROJECT_ROOT/SurahCardView.swift" "$PROJECT_ROOT/Presentation/Quran/QuranList/Views/"

# Presentation - Surah Detail ViewModels
[ -f "$PROJECT_ROOT/SurahNavigator.swift" ] && mv "$PROJECT_ROOT/SurahNavigator.swift" "$PROJECT_ROOT/Presentation/Quran/SurahDetail/ViewModels/"
[ -f "$PROJECT_ROOT/SurahContentViewModel.swift" ] && mv "$PROJECT_ROOT/SurahContentViewModel.swift" "$PROJECT_ROOT/Presentation/Quran/SurahDetail/ViewModels/"
[ -f "$PROJECT_ROOT/ReadingSettingsViewModel.swift" ] && mv "$PROJECT_ROOT/ReadingSettingsViewModel.swift" "$PROJECT_ROOT/Presentation/Quran/SurahDetail/ViewModels/"
[ -f "$PROJECT_ROOT/SurahDetailCoordinator.swift" ] && mv "$PROJECT_ROOT/SurahDetailCoordinator.swift" "$PROJECT_ROOT/Presentation/Quran/SurahDetail/ViewModels/"

# Presentation - Surah Detail Views
[ -f "$PROJECT_ROOT/SurahDetailView.swift" ] && mv "$PROJECT_ROOT/SurahDetailView.swift" "$PROJECT_ROOT/Presentation/Quran/SurahDetail/Views/"
[ -f "$PROJECT_ROOT/SurahHeaderView.swift" ] && mv "$PROJECT_ROOT/SurahHeaderView.swift" "$PROJECT_ROOT/Presentation/Quran/SurahDetail/Views/"
[ -f "$PROJECT_ROOT/BismillahView.swift" ] && mv "$PROJECT_ROOT/BismillahView.swift" "$PROJECT_ROOT/Presentation/Quran/SurahDetail/Views/"
[ -f "$PROJECT_ROOT/ReadingSettingsSheet.swift" ] && mv "$PROJECT_ROOT/ReadingSettingsSheet.swift" "$PROJECT_ROOT/Presentation/Quran/SurahDetail/Views/"
[ -f "$PROJECT_ROOT/AyahCardView.swift" ] && mv "$PROJECT_ROOT/AyahCardView.swift" "$PROJECT_ROOT/Presentation/Quran/SurahDetail/Views/"
[ -f "$PROJECT_ROOT/ArabicOnlyView.swift" ] && mv "$PROJECT_ROOT/ArabicOnlyView.swift" "$PROJECT_ROOT/Presentation/Quran/SurahDetail/Views/"

# Core - DependencyInjection
[ -f "$PROJECT_ROOT/DependencyContainer.swift" ] && mv "$PROJECT_ROOT/DependencyContainer.swift" "$PROJECT_ROOT/Core/DependencyInjection/"

# Core - Navigation
[ -f "$PROJECT_ROOT/AppRouter.swift" ] && mv "$PROJECT_ROOT/AppRouter.swift" "$PROJECT_ROOT/Core/Navigation/"
[ -f "$PROJECT_ROOT/AppTab.swift" ] && mv "$PROJECT_ROOT/AppTab.swift" "$PROJECT_ROOT/Core/Navigation/"
[ -f "$PROJECT_ROOT/MainTabView.swift" ] && mv "$PROJECT_ROOT/MainTabView.swift" "$PROJECT_ROOT/Core/Navigation/"

# Core - Services
[ -f "$PROJECT_ROOT/LocationService.swift" ] && mv "$PROJECT_ROOT/LocationService.swift" "$PROJECT_ROOT/Core/Services/"

# Core - Utilities
[ -f "$PROJECT_ROOT/AppViewModel.swift" ] && mv "$PROJECT_ROOT/AppViewModel.swift" "$PROJECT_ROOT/Core/Utilities/"

# Tests
[ -f "$PROJECT_ROOT/RefactoredArchitectureTests.swift" ] && mv "$PROJECT_ROOT/RefactoredArchitectureTests.swift" "$PROJECT_ROOT/Tests/PresentationTests/ArchitectureTests.swift"

# Documentation
mv "$PROJECT_ROOT"/*.md "$PROJECT_ROOT/Documentation/" 2>/dev/null

echo "✅ Files moved!"
echo ""

# Delete legacy files
echo "🗑️  Deleting legacy files..."

[ -f "$PROJECT_ROOT/SurahDetailViewModel.swift" ] && rm "$PROJECT_ROOT/SurahDetailViewModel.swift" && echo "  ❌ Deleted SurahDetailViewModel.swift"
[ -f "$PROJECT_ROOT/ReadingPreferences.swift" ] && rm "$PROJECT_ROOT/ReadingPreferences.swift" && echo "  ❌ Deleted ReadingPreferences.swift"
[ -f "$PROJECT_ROOT/RefactoredSyncQuranDataUseCase.swift" ] && rm "$PROJECT_ROOT/RefactoredSyncQuranDataUseCase.swift" && echo "  ❌ Deleted RefactoredSyncQuranDataUseCase.swift"
[ -f "$PROJECT_ROOT/RefactoredFetchAyahsUseCase.swift" ] && rm "$PROJECT_ROOT/RefactoredFetchAyahsUseCase.swift" && echo "  ❌ Deleted RefactoredFetchAyahsUseCase.swift"
[ -f "$PROJECT_ROOT/RefactoredReadingSettingsSheet.swift" ] && rm "$PROJECT_ROOT/RefactoredReadingSettingsSheet.swift" && echo "  ❌ Deleted RefactoredReadingSettingsSheet.swift"

echo ""
echo "✅ Legacy files deleted!"
echo ""

# Summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎉 PROJECT ORGANIZATION COMPLETE!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📂 Your project is now organized into:"
echo "   ✅ App/"
echo "   ✅ Domain/"
echo "   ✅ Data/"
echo "   ✅ Presentation/"
echo "   ✅ Core/"
echo "   ✅ Tests/"
echo "   ✅ Documentation/"
echo ""
echo "⚠️  IMPORTANT: You still need to:"
echo "   1. Open your project in Xcode"
echo "   2. Remove old file references (if any show as red)"
echo "   3. Add new folders to Xcode project"
echo "   4. Build and test (⌘+B)"
echo ""
echo "📖 See Documentation/PROJECT_FOLDER_STRUCTURE.md for details"
echo ""
echo "✨ Happy coding!"
