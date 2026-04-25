package com.iaspring.backspring.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.iaspring.backspring.entity.Utilisateur;
import com.iaspring.backspring.repository.UtilisateurRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String pseudo = oAuth2User.getAttribute("name");
        
        if (email == null) {
            email = oAuth2User.getAttribute("login") + "@github.com";
            pseudo = oAuth2User.getAttribute("login");
        }

        final String finalEmail = email;
        final String finalPseudo = pseudo;
        
        Utilisateur utilisateur = utilisateurRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    Utilisateur newUser = new Utilisateur();
                    newUser.setEmail(finalEmail);
                    newUser.setPseudo(finalPseudo != null ? finalPseudo : "Joueur_" + finalEmail.split("@")[0]);
                    newUser.setMotDePasse(UUID.randomUUID().toString());
                    newUser.setRole("ROLE_USER");
                    return utilisateurRepository.save(newUser);
                });

        String token = jwtService.genererToken(utilisateur);

        // 👉 CORRECTION : On passe l'ID, le Pseudo et le Rôle dans l'URL (avec encodage pour les espaces)
        String pseudoEncode = java.net.URLEncoder.encode(utilisateur.getPseudo(), java.nio.charset.StandardCharsets.UTF_8);
        String targetUrl = "http://localhost:4200/login?token=" + token 
                         + "&id=" + utilisateur.getId() 
                         + "&pseudo=" + pseudoEncode 
                         + "&role=" + utilisateur.getRole();
                         
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}