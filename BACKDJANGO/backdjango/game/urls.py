from django.urls import path, include
from rest_framework.routers import DefaultRouter

from .views import (
    AppliquerEffetAPIView, EtatPartieAPIView, JeuAPIView, PreparerSuperBonusAPIView, 
    RepondreAPIView, CasePlateauViewSet, RepondreSuperBonusAPIView,GithubLoginView, 
    GithubCallbackView, UtilisateurViewSet, QuestionViewSet, PartieViewSet, 
    PartieJoueurViewSet, HistoriqueQuestionViewSet, AuthAPIView, RegisterAPIView, 
    GoogleLoginView, GoogleCallbackView,
)

# 1. Création du routeur automatique pour nos vues CRUD
router = DefaultRouter()
router.register(r'utilisateurs', UtilisateurViewSet, basename='utilisateur')
router.register(r'questions', QuestionViewSet, basename='question')
# Change 'admin-parties' par 'parties' pour correspondre à Angular
router.register(r'parties', PartieViewSet, basename='partie')
router.register(r'partie-joueurs', PartieJoueurViewSet, basename='partie_joueur')
router.register(r'historique', HistoriqueQuestionViewSet)
router.register(r'plateau', CasePlateauViewSet, basename='plateau')

# 2. Définition des routes manuelles pour le jeu
urlpatterns = [
    # --- Routes du Moteur de Jeu ---
    path('jeu/demarrer/', JeuAPIView.as_view(), name='demarrer_jeu'),
    path('jeu/question/<int:partie_id>/<int:joueur_id>/', JeuAPIView.as_view(), name='obtenir_question'),
    path('jeu/repondre/', RepondreAPIView.as_view(), name='repondre_question'),
    path('jeu/etat/<int:partie_id>/', EtatPartieAPIView.as_view(), name='etat_partie'),
    path('jeu/appliquer-effet/', AppliquerEffetAPIView.as_view(), name='appliquer_effet'),
    # 👉 NOUVELLES ROUTES SUPER BONUS
    path('jeu/preparer-super-bonus/', PreparerSuperBonusAPIView.as_view(), name='preparer_super_bonus'),
    path('jeu/repondre-super-bonus/', RepondreSuperBonusAPIView.as_view(), name='repondre_super_bonus'),
    
    # --- Route d'Authentification ---
    path('auth/login/', AuthAPIView.as_view(), name='auth_login'), # 👉 LA NOUVELLE ROUTE
    
    # --- Routes d'Authentification ---
    path('auth/login/', AuthAPIView.as_view(), name='login_classique'),
    path('auth/register/', RegisterAPIView.as_view(), name='register_classique'),
    
    path('auth/google/login/', GoogleLoginView.as_view(), name='google_login'),
    path('auth/google/callback/', GoogleCallbackView.as_view(), name='google_callback'),
    
    path('auth/github/login/', GithubLoginView.as_view(), name='github_login'),
    path('auth/github/callback/', GithubCallbackView.as_view(), name='github_callback'),
    
    # --- Inclusion de toutes les routes CRUD générées par le routeur ---
    path('', include(router.urls)),
]