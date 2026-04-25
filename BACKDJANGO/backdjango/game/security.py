import jwt
import datetime
from rest_framework import authentication
from rest_framework.permissions import BasePermission
from .models import Utilisateur

# La clé secrète pour signer les tokens (équivalent du SECRET_KEY de Spring)
SECRET_KEY = "TaCleSecreteUltraSecuriseeChangeLaEnProduction"

class JwtService:
    @staticmethod
    def generer_token(utilisateur):
        # 👉 NOUVEAU : On utilise la méthode moderne pour l'heure UTC
        maintenant = datetime.datetime.now(datetime.timezone.utc)
        
        payload = {
            'sub': utilisateur.email,
            'role': utilisateur.role,
            'pseudo': utilisateur.pseudo,
            'exp': maintenant + datetime.timedelta(days=1),
            'iat': maintenant
        }
        return jwt.encode(payload, SECRET_KEY, algorithm='HS256')

# --- LE FILTRE (Le Videur) ---
class JWTAuthentication(authentication.BaseAuthentication):
    def authenticate(self, request):
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswith('Bearer '):
            return None # Pas de token, on le laisse passer comme visiteur normal

        token = auth_header.split(' ')[1]
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=['HS256'])
            user = Utilisateur.objects.get(email=payload['sub'])
            return (user, token) # On a reconnu l'utilisateur !
        except Exception:
            return None

# --- LA RÈGLE DE SÉCURITÉ ---
class IsAdminRole(BasePermission):
    def has_permission(self, request, view):
        # Vérifie si l'utilisateur est connecté ET s'il a le rôle ADMIN
        if not request.user or not hasattr(request.user, 'role'):
            return False
        return request.user.role == 'ROLE_ADMIN'