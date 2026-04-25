-- =====================================================================
-- SCRIPT D'INSERTION DES 120 QUESTIONS (Qualité Artisanale)
-- 6 Catégories | 10 TEXTE et 10 QCM par catégorie
-- =====================================================================

-- ----------------------------------------------------
-- 1. HISTOIRE
-- ----------------------------------------------------
-- TEXTE (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('HISTOIRE', 'TEXTE', 'PERSONNE', 'Qui était le premier empereur des Français ?', 'Napoléon Bonaparte', NULL, NULL, NULL, 'Napoléon, Bonaparte, Napoleon 1er, Napoleon Ier'),
('HISTOIRE', 'TEXTE', 'PERSONNE', 'Comment s''appelait le premier président des États-Unis ?', 'George Washington', NULL, NULL, NULL, 'Washington, G. Washington'),
('HISTOIRE', 'TEXTE', 'PERSONNE', 'Quel peuple d''Amérique du Sud a construit le Machu Picchu ?', 'Les Incas', NULL, NULL, NULL, 'Incas, L''empire Inca, Peuple Inca,  Inka, Inca, Inqua, Hinca, Hinka, Hinqua'),
('HISTOIRE', 'TEXTE', 'PERSONNE', 'Quelle célèbre reine d''Égypte a eu une liaison avec Jules César ?', 'Cléopâtre', NULL, NULL, NULL, 'Cleopatre, Cléopâtre VII'),
('HISTOIRE', 'TEXTE', 'PERSONNE', 'Quel explorateur a découvert l''Amérique en 1492 ?', 'Christophe Colomb', NULL, NULL, NULL, 'Colomb, Christopher Columbus'),
('HISTOIRE', 'TEXTE', 'PERSONNE', 'Quel était le surnom de l''héroïne française qui a libéré Orléans en 1429 ?', 'La Pucelle d''Orléans', NULL, NULL, NULL, 'Jeanne d''Arc, La pucelle, Pucelle d''Orleans'),
('HISTOIRE', 'TEXTE', 'LIEU', 'Dans quel pays a commencé la Révolution industrielle au 18ème siècle ?', 'Royaume-Uni', NULL, NULL, NULL, 'Angleterre, Grande-Bretagne, UK, United Kingdom'),
('HISTOIRE', 'TEXTE', 'PERSONNE', 'Qui a été le chef de file de la Révolution russe de 1917 ?', 'Lénine', NULL, NULL, NULL, 'Lenine, Vladimir Ilitch Oulianov, Lenin'),
('HISTOIRE', 'TEXTE', 'LIEU', 'Quel célèbre mur est tombé le 9 novembre 1989 ?', 'Le mur de Berlin', NULL, NULL, NULL, 'Mur de Berlin, Berlin wall, berlin'),
('HISTOIRE', 'TEXTE', 'LIEU', 'Quel était l''ancien nom de la ville d''Istanbul avant 1868 ?', 'Constantinople', NULL, NULL, NULL, 'Byzance, Nouvelle Rome');

-- QCM (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('HISTOIRE', 'QCM_4', 'DATE', 'En quelle année a eu lieu la prise de la Bastille ?', '1789', '1799', '1776', '1804', NULL),
('HISTOIRE', 'QCM_4', 'PERSONNE', 'Quel roi de France était surnommé le Roi-Soleil ?', 'Louis XIV', 'Louis XV', 'Henri IV', 'François Ier', NULL),
('HISTOIRE', 'QCM_4', 'DATE', 'En quelle année a commencé la Première Guerre mondiale ?', '1914', '1939', '1870', '1918', NULL),
('HISTOIRE', 'QCM_2', 'CONCEPT', 'Vrai ou Faux : Jeanne d''Arc a été brûlée à Rouen.', 'Vrai', 'Faux', NULL, NULL, NULL),
('HISTOIRE', 'QCM_4', 'PERSONNE', 'Qui a ordonné la construction du Colisée à Rome ?', 'L''empereur Vespasien', 'L''empereur Néron', 'Jules César', 'Auguste', NULL),
('HISTOIRE', 'QCM_4', 'DATE', 'Quand a pris fin la Seconde Guerre mondiale en Europe ?', '1945', '1944', '1918', '1989', NULL),
('HISTOIRE', 'QCM_4', 'PERSONNE', 'Qui était le Premier ministre britannique durant la Seconde Guerre mondiale ?', 'Winston Churchill', 'Neville Chamberlain', 'Margaret Thatcher', 'Tony Blair', NULL),
('HISTOIRE', 'QCM_4', 'LIEU', 'Dans quelle ville a été signé le traité mettant fin à la Première Guerre mondiale ?', 'Versailles', 'Paris', 'Berlin', 'Vienne', NULL),
('HISTOIRE', 'QCM_4', 'PERSONNE', 'Quel pharaon est célèbre pour son masque funéraire en or massif découvert en 1922 ?', 'Toutânkhamon', 'Ramsès II', 'Khéops', 'Akhenaton', NULL),
('HISTOIRE', 'QCM_4', 'DATE', 'En quelle année l''homme a-t-il marché sur la Lune pour la première fois ?', '1969', '1961', '1972', '1958', NULL);

-- ----------------------------------------------------
-- 2. GÉOGRAPHIE
-- ----------------------------------------------------
-- TEXTE (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('GEOGRAPHIE', 'TEXTE', 'LIEU', 'Quel est le plus petit pays du monde ?', 'Le Vatican', NULL, NULL, NULL, 'Vatican, État de la Cité du Vatican'),
('GEOGRAPHIE', 'TEXTE', 'LIEU', 'Quelle est la capitale du Japon ?', 'Tokyo', NULL, NULL, NULL, 'Tokio'),
('GEOGRAPHIE', 'TEXTE', 'LIEU', 'Quel grand désert se trouve au nord de l''Afrique ?', 'Le Sahara', NULL, NULL, NULL, 'Sahara'),
('GEOGRAPHIE', 'TEXTE', 'LIEU', 'Comment s''appelle le fleuve qui traverse Londres ?', 'La Tamise', NULL, NULL, NULL, 'Tamise, Thames'),
('GEOGRAPHIE', 'TEXTE', 'LIEU', 'Dans quel pays se trouve la ville de Rio de Janeiro ?', 'Le Brésil', NULL, NULL, NULL, 'Brésil, Bresil, Brazil'),
('GEOGRAPHIE', 'TEXTE', 'LIEU', 'Quelle est la capitale de l''Espagne ?', 'Madrid', NULL, NULL, NULL, 'Madrid'),
('GEOGRAPHIE', 'TEXTE', 'LIEU', 'Quel océan se trouve entre l''Europe et l''Amérique du Nord ?', 'L''océan Atlantique', NULL, NULL, NULL, 'Atlantique, Ocean Atlantique'),
('GEOGRAPHIE', 'TEXTE', 'LIEU', 'Quel est le plus haut sommet d''Europe occidentale ?', 'Le Mont Blanc', NULL, NULL, NULL, 'Mont Blanc, Mont-Blanc'),
('GEOGRAPHIE', 'TEXTE', 'LIEU', 'Quelle grande île se situe au large de la côte sud-est de l''Afrique ?', 'Madagascar', NULL, NULL, NULL, 'Ile de Madagascar'),
('GEOGRAPHIE', 'TEXTE', 'NOMBRE', 'Combien de continents y a-t-il sur Terre (selon le modèle le plus courant) ?', '7', NULL, NULL, NULL, 'Sept, 7 continents');

-- QCM (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('GEOGRAPHIE', 'QCM_4', 'LIEU', 'Quelle est la capitale de l''Australie ?', 'Canberra', 'Sydney', 'Melbourne', 'Brisbane', NULL),
('GEOGRAPHIE', 'QCM_4', 'LIEU', 'Quel est le plus long fleuve du monde ?', 'L''Amazone', 'Le Nil', 'Le Yangzi Jiang', 'Le Mississippi', NULL),
('GEOGRAPHIE', 'QCM_4', 'LIEU', 'Dans quel pays se trouve le mont Kilimandjaro ?', 'Tanzanie', 'Kenya', 'Afrique du Sud', 'Maroc', NULL),
('GEOGRAPHIE', 'QCM_4', 'LIEU', 'Quelle est la capitale du Canada ?', 'Ottawa', 'Toronto', 'Montréal', 'Vancouver', NULL),
('GEOGRAPHIE', 'QCM_4', 'LIEU', 'Quel est le plus grand pays du monde par sa superficie ?', 'La Russie', 'Le Canada', 'La Chine', 'Les États-Unis', NULL),
('GEOGRAPHIE', 'QCM_2', 'CONCEPT', 'Vrai ou Faux : L''Islande est recouverte à plus de 80 % de glace.', 'Faux', 'Vrai', NULL, NULL, NULL),
('GEOGRAPHIE', 'QCM_4', 'NOMBRE', 'Combien d''États composent les États-Unis d''Amérique ?', '50', '52', '48', '51', NULL),
('GEOGRAPHIE', 'QCM_4', 'LIEU', 'Dans quel océan se trouvent les îles Maldives ?', 'L''océan Indien', 'L''océan Pacifique', 'L''océan Atlantique', 'L''océan Arctique', NULL),
('GEOGRAPHIE', 'QCM_4', 'LIEU', 'Quelle mer borde la côte sud de la France ?', 'La mer Méditerranée', 'L''océan Atlantique', 'La Manche', 'La mer du Nord', NULL),
('GEOGRAPHIE', 'QCM_4', 'LIEU', 'Quel est le pays le plus peuplé du monde en 2023 ?', 'L''Inde', 'La Chine', 'Les États-Unis', 'L''Indonésie', NULL);

-- ----------------------------------------------------
-- 3. SCIENCES
-- ----------------------------------------------------
-- TEXTE (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('SCIENCES', 'TEXTE', 'OBJET', 'Quel est le symbole chimique de l''Or ?', 'Au', NULL, NULL, NULL, 'Or, Au'),
('SCIENCES', 'TEXTE', 'CONCEPT', 'Quel gaz les plantes absorbent-elles lors de la photosynthèse ?', 'Le dioxyde de carbone', NULL, NULL, NULL, 'CO2, Gaz carbonique, Dioxyde de carbone'),
('SCIENCES', 'TEXTE', 'LIEU', 'Comment s''appelle notre galaxie ?', 'La Voie lactée', NULL, NULL, NULL, 'Voie lactée, Milky Way'),
('SCIENCES', 'TEXTE', 'CONCEPT', 'Quelle est l''unité de mesure de la tension électrique ?', 'Le Volt', NULL, NULL, NULL, 'Volt, V, Volts'),
('SCIENCES', 'TEXTE', 'OBJET', 'Quel organe du corps humain produit l''insuline ?', 'Le pancréas', NULL, NULL, NULL, 'Pancréas, Pancreas'),
('SCIENCES', 'TEXTE', 'OBJET', 'Quelle molécule est universellement connue sous la formule H2O ?', 'L''eau', NULL, NULL, NULL, 'Eau'),
('SCIENCES', 'TEXTE', 'PERSONNE', 'Qui a découvert la pénicilline en 1928 ?', 'Alexander Fleming', NULL, NULL, NULL, 'Fleming, A. Fleming'),
('SCIENCES', 'TEXTE', 'OBJET', 'Quelles cellules du sang sont responsables du transport de l''oxygène ?', 'Les globules rouges', NULL, NULL, NULL, 'Globules rouges, Érythrocytes, Hématies'),
('SCIENCES', 'TEXTE', 'LIEU', 'Quelle planète du système solaire est surnommée la planète rouge ?', 'Mars', NULL, NULL, NULL, 'La planète Mars'),
('SCIENCES', 'TEXTE', 'CONCEPT', 'Comment appelle-t-on la force qui attire les objets vers le centre de la Terre ?', 'La gravité', NULL, NULL, NULL, 'Gravité, L''attraction terrestre, Pesanteur');

-- QCM (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('SCIENCES', 'QCM_4', 'LIEU', 'Quelle est la planète la plus proche du Soleil ?', 'Mercure', 'Vénus', 'Mars', 'Jupiter', NULL),
('SCIENCES', 'QCM_4', 'OBJET', 'Quel est le seul métal liquide à température ambiante ?', 'Le mercure', 'Le fer', 'L''or', 'Le cuivre', NULL),
('SCIENCES', 'QCM_4', 'PERSONNE', 'Qui a formulé la théorie de la relativité restreinte ?', 'Albert Einstein', 'Isaac Newton', 'Galilée', 'Nikola Tesla', NULL),
('SCIENCES', 'QCM_4', 'NOMBRE', 'Combien de chromosomes compte l''espèce humaine en temps normal ?', '46', '48', '44', '42', NULL),
('SCIENCES', 'QCM_4', 'OBJET', 'De quoi est principalement composé le Soleil ?', 'D''hydrogène', 'D''oxygène', 'De carbone', 'D''azote', NULL),
('SCIENCES', 'QCM_4', 'PERSONNE', 'Quelle célèbre scientifique a découvert le radium et le polonium ?', 'Marie Curie', 'Rosalind Franklin', 'Ada Lovelace', 'Lise Meitner', NULL),
('SCIENCES', 'QCM_4', 'OBJET', 'Quel est l''os le plus long du corps humain ?', 'Le fémur', 'Le tibia', 'L''humérus', 'Le péroné', NULL),
('SCIENCES', 'QCM_2', 'CONCEPT', 'Vrai ou Faux : Le son voyage plus vite dans l''eau que dans l''air.', 'Vrai', 'Faux', NULL, NULL, NULL),
('SCIENCES', 'QCM_4', 'CONCEPT', 'Quel phénomène naturel est mesuré sur l''échelle de Richter ?', 'Les séismes', 'Les ouragans', 'Les éruptions volcaniques', 'Les tsunamis', NULL),
('SCIENCES', 'QCM_4', 'OBJET', 'Quel est le gaz le plus abondant dans l''atmosphère terrestre ?', 'L''azote', 'L''oxygène', 'Le dioxyde de carbone', 'L''hélium', NULL);

-- ----------------------------------------------------
-- 4. DIVERTISSEMENT
-- ----------------------------------------------------
-- TEXTE (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('DIVERTISSEMENT', 'TEXTE', 'PERSONNE', 'Quel est le nom du sorcier à lunettes créé par J.K. Rowling ?', 'Harry Potter', NULL, NULL, NULL, 'Potter, Harry'),
('DIVERTISSEMENT', 'TEXTE', 'PERSONNE', 'Quel est le prénom du célèbre plombier rouge de Nintendo ?', 'Mario', NULL, NULL, NULL, 'Super Mario, Mario Bros'),
('DIVERTISSEMENT', 'TEXTE', 'PERSONNE', 'Quel groupe de rock britannique a chanté "Bohemian Rhapsody" ?', 'Queen', NULL, NULL, NULL, 'The Queen'),
('DIVERTISSEMENT', 'TEXTE', 'PERSONNE', 'Dans le Seigneur des Anneaux, comment s''appelle le magicien gris ?', 'Gandalf', NULL, NULL, NULL, 'Gandalf le gris, Mithrandir'),
('DIVERTISSEMENT', 'TEXTE', 'PERSONNE', 'Quel super-héros DC vit dans la ville de Gotham City ?', 'Batman', NULL, NULL, NULL, 'L''homme chauve-souris, Bruce Wayne'),
('DIVERTISSEMENT', 'TEXTE', 'PERSONNE', 'Quel réalisateur a créé le film "Jurassic Park" en 1993 ?', 'Steven Spielberg', NULL, NULL, NULL, 'Spielberg, S. Spielberg'),
('DIVERTISSEMENT', 'TEXTE', 'LIEU', 'Dans quelle ville fictive se déroule la série animée Les Simpson ?', 'Springfield', NULL, NULL, NULL, 'Springfield'),
('DIVERTISSEMENT', 'TEXTE', 'PERSONNE', 'Quel Pokémon électrique jaune est le compagnon de Sacha ?', 'Pikachu', NULL, NULL, NULL, 'Pikachu'),
('DIVERTISSEMENT', 'TEXTE', 'OEUVRE', 'Quel film de science-fiction met en scène Neo et Morpheus ?', 'Matrix', NULL, NULL, NULL, 'The Matrix, La Matrice'),
('DIVERTISSEMENT', 'TEXTE', 'PERSONNE', 'Quelle célèbre souris de dessin animé a été créée par Walt Disney en 1928 ?', 'Mickey Mouse', NULL, NULL, NULL, 'Mickey');

-- QCM (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('DIVERTISSEMENT', 'QCM_4', 'PERSONNE', 'Dans Star Wars, qui est le père de Luke Skywalker ?', 'Dark Vador', 'Obi-Wan Kenobi', 'Maître Yoda', 'L''Empereur Palpatine', NULL),
('DIVERTISSEMENT', 'QCM_4', 'PERSONNE', 'Quel acteur joue Jack Dawson dans le film Titanic ?', 'Leonardo DiCaprio', 'Brad Pitt', 'Johnny Depp', 'Tom Cruise', NULL),
('DIVERTISSEMENT', 'QCM_4', 'PERSONNE', 'Dans la série Friends, comment s''appelle le frère de Monica ?', 'Ross', 'Chandler', 'Joey', 'Gunther', NULL),
('DIVERTISSEMENT', 'QCM_4', 'OEUVRE', 'Quel est le jeu vidéo le plus vendu de tous les temps ?', 'Minecraft', 'Grand Theft Auto V', 'Tetris', 'Wii Sports', NULL),
('DIVERTISSEMENT', 'QCM_4', 'NOMBRE', 'Combien de films composent la trilogie originale du Seigneur des Anneaux ?', '3', '4', '2', '5', NULL),
('DIVERTISSEMENT', 'QCM_4', 'PERSONNE', 'Quelle chanteuse est connue pour son tube "Rolling in the Deep" ?', 'Adele', 'Beyoncé', 'Taylor Swift', 'Lady Gaga', NULL),
('DIVERTISSEMENT', 'QCM_2', 'CONCEPT', 'Vrai ou Faux : Le personnage de James Bond a été créé par Ian Fleming.', 'Vrai', 'Faux', NULL, NULL, NULL),
('DIVERTISSEMENT', 'QCM_4', 'OEUVRE', 'Quelle série met en scène la lutte pour le Trône de Fer ?', 'Game of Thrones', 'Le Seigneur des Anneaux', 'The Witcher', 'Vikings', NULL),
('DIVERTISSEMENT', 'QCM_4', 'LIEU', 'Dans l''univers d''Harry Potter, comment s''appelle l''école de magie ?', 'Poudlard', 'Beauxbâtons', 'Durmstrang', 'Ilvermorny', NULL),
('DIVERTISSEMENT', 'QCM_4', 'PERSONNE', 'Quel artiste a sorti l''album "Thriller", l''un des plus vendus au monde ?', 'Michael Jackson', 'Prince', 'Elvis Presley', 'Madonna', NULL);

-- ----------------------------------------------------
-- 5. SPORT
-- ----------------------------------------------------
-- TEXTE (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('SPORT', 'TEXTE', 'LIEU', 'Quel pays a remporté la toute première Coupe du Monde de football en 1930 ?', 'L''Uruguay', NULL, NULL, NULL, 'Uruguay'),
('SPORT', 'TEXTE', 'CONCEPT', 'Dans quel sport de raquette utilise-t-on un objet appelé "volant" ?', 'Le badminton', NULL, NULL, NULL, 'Badminton'),
('SPORT', 'TEXTE', 'PERSONNE', 'Quelle est la nationalité du joueur de tennis Rafael Nadal ?', 'Espagnol', NULL, NULL, NULL, 'Espagne, Espagnole'),
('SPORT', 'TEXTE', 'CONCEPT', 'Quel sport martial japonais signifie "la voie de la souplesse" ?', 'Le Judo', NULL, NULL, NULL, 'Judo'),
('SPORT', 'TEXTE', 'PERSONNE', 'Quel basketteur américain portait le numéro 23 chez les Chicago Bulls ?', 'Michael Jordan', NULL, NULL, NULL, 'Jordan, M. Jordan'),
('SPORT', 'TEXTE', 'OBJET', 'Comment appelle-t-on la récompense attribuée au meilleur buteur européen de l''année ?', 'Le Soulier d''or', NULL, NULL, NULL, 'Soulier d''or, Soulier d or'),
('SPORT', 'TEXTE', 'LIEU', 'Dans quelle ville se déroule le célèbre tournoi de tennis sur gazon de Wimbledon ?', 'Londres', NULL, NULL, NULL, 'London'),
('SPORT', 'TEXTE', 'CONCEPT', 'Quel sport consiste à lancer une lourde boule pour renverser dix quilles ?', 'Le bowling', NULL, NULL, NULL, 'Bowling'),
('SPORT', 'TEXTE', 'PERSONNE', 'Quel joueur argentin a remporté le ballon d''or en 2022 ?', 'Lionel Messi', NULL, NULL, NULL, 'Messi, Leo Messi'),
('SPORT', 'TEXTE', 'PERSONNE', 'Quel coureur jamaïcain détient le record du monde du 100 mètres ?', 'Usain Bolt', NULL, NULL, NULL, 'Bolt, U. Bolt');

-- QCM (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('SPORT', 'QCM_4', 'NOMBRE', 'Quelle est la distance officielle d''un marathon en athlétisme ?', '42,195 km', '40 km', '45 km', '41,500 km', NULL),
('SPORT', 'QCM_4', 'CONCEPT', 'Dans quel sport s''illustre principalement le Français Antoine Dupont ?', 'Le rugby', 'Le football', 'Le handball', 'Le tennis', NULL),
('SPORT', 'QCM_4', 'NOMBRE', 'Combien de joueurs composent une équipe de football sur le terrain ?', '11', '10', '12', '15', NULL),
('SPORT', 'QCM_4', 'PERSONNE', 'Quel footballeur brésilien est surnommé "Le Roi" (O Rei) ?', 'Pelé', 'Ronaldo', 'Ronaldinho', 'Neymar', NULL),
('SPORT', 'QCM_4', 'LIEU', 'Quel tournoi du Grand Chelem se joue sur terre battue à Paris ?', 'Roland-Garros', 'Wimbledon', 'US Open', 'Open d''Australie', NULL),
('SPORT', 'QCM_4', 'PERSONNE', 'Qui est considéré comme l''un des plus grands joueurs de golf de tous les temps ?', 'Tiger Woods', 'Roger Federer', 'LeBron James', 'Tom Brady', NULL),
('SPORT', 'QCM_2', 'CONCEPT', 'Vrai ou Faux : Les Jeux Olympiques d''été ont lieu tous les quatre ans.', 'Vrai', 'Faux', NULL, NULL, NULL),
('SPORT', 'QCM_4', 'NOMBRE', 'Combien de temps dure un match de rugby à XV (sans prolongations) ?', '80 minutes', '90 minutes', '60 minutes', '100 minutes', NULL),
('SPORT', 'QCM_4', 'PERSONNE', 'Quel pilote britannique détient le record de victoires en Formule 1 ?', 'Lewis Hamilton', 'Michael Schumacher', 'Ayrton Senna', 'Max Verstappen', NULL),
('SPORT', 'QCM_4', 'CONCEPT', 'Quel sport se pratique sur une patinoire avec un palet ?', 'Le hockey sur glace', 'Le patinage artistique', 'Le curling', 'Le bobsleigh', NULL);

-- ----------------------------------------------------
-- 6. ARTS ET LITTERATURE
-- ----------------------------------------------------
-- TEXTE (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('ARTS_ET_LITTERATURE', 'TEXTE', 'PERSONNE', 'Qui est l''auteur du célèbre roman "Les Misérables" ?', 'Victor Hugo', NULL, NULL, NULL, 'Hugo, V. Hugo'),
('ARTS_ET_LITTERATURE', 'TEXTE', 'LIEU', 'Quel musée parisien est célèbre pour sa grande pyramide de verre ?', 'Le Louvre', NULL, NULL, NULL, 'Louvre, Musée du Louvre'),
('ARTS_ET_LITTERATURE', 'TEXTE', 'PERSONNE', 'Quel dramaturge anglais a écrit la tragédie "Roméo et Juliette" ?', 'William Shakespeare', NULL, NULL, NULL, 'Shakespeare, W. Shakespeare'),
('ARTS_ET_LITTERATURE', 'TEXTE', 'PERSONNE', 'Quel artiste espagnol est connu pour ses toiles surréalistes avec des horloges molles ?', 'Salvador Dalí', NULL, NULL, NULL, 'Dali, Salvador Dali'),
('ARTS_ET_LITTERATURE', 'TEXTE', 'PERSONNE', 'Quel dramaturge français a écrit la comédie "Le Tartuffe" ?', 'Molière', NULL, NULL, NULL, 'Moliere, Jean-Baptiste Poquelin'),
('ARTS_ET_LITTERATURE', 'TEXTE', 'PERSONNE', 'Quel romancier français est l''auteur de "Germinal" ?', 'Émile Zola', NULL, NULL, NULL, 'Zola, Emile Zola'),
('ARTS_ET_LITTERATURE', 'TEXTE', 'PERSONNE', 'Qui est le célèbre compositeur autrichien du chef-d''œuvre "La Flûte enchantée" ?', 'Wolfgang Amadeus Mozart', NULL, NULL, NULL, 'Mozart, W.A. Mozart'),
('ARTS_ET_LITTERATURE', 'TEXTE', 'PERSONNE', 'Quel poète français a écrit le recueil "Les Fleurs du mal" ?', 'Charles Baudelaire', NULL, NULL, NULL, 'Baudelaire, C. Baudelaire'),
('ARTS_ET_LITTERATURE', 'TEXTE', 'LIEU', 'Dans quel musée de Paris peut-on admirer la plus grande collection de peintures impressionnistes ?', 'Le Musée d''Orsay', NULL, NULL, NULL, 'Musée d''Orsay, Orsay, orsai, orsé'),
('ARTS_ET_LITTERATURE', 'TEXTE', 'PERSONNE', 'Quel peintre espagnol est l''un des fondateurs du mouvement cubiste ?', 'Pablo Picasso', NULL, NULL, NULL, 'Picasso, P. Picasso');

-- QCM (10)
INSERT INTO QUESTION (categorie, type_question, type_reponse, texte_question, bonne_reponse, mauvaise_prop_1, mauvaise_prop_2, mauvaise_prop_3, synonymes_acceptes) VALUES 
('ARTS_ET_LITTERATURE', 'QCM_4', 'PERSONNE', 'Qui a peint le célèbre tableau "La Joconde" ?', 'Léonard de Vinci', 'Michel-Ange', 'Vincent van Gogh', 'Pablo Picasso', NULL),
('ARTS_ET_LITTERATURE', 'QCM_4', 'PERSONNE', 'Dans la mythologie grecque, qui est le dieu souverain des Enfers ?', 'Hadès', 'Zeus', 'Poséidon', 'Apollon', NULL),
('ARTS_ET_LITTERATURE', 'QCM_4', 'PERSONNE', 'Quel peintre néerlandais est l''auteur du tableau "La Nuit étoilée" ?', 'Vincent van Gogh', 'Rembrandt', 'Johannes Vermeer', 'Claude Monet', NULL),
('ARTS_ET_LITTERATURE', 'QCM_4', 'PERSONNE', 'Quel écrivain a publié le roman philosophique "L''Étranger" en 1942 ?', 'Albert Camus', 'Jean-Paul Sartre', 'Marcel Proust', 'Antoine de Saint-Exupéry', NULL),
('ARTS_ET_LITTERATURE', 'QCM_4', 'OEUVRE', 'Laquelle de ces œuvres a été peinte par Pablo Picasso ?', 'Guernica', 'Le Cri', 'La Cène', 'Impression, soleil levant', NULL),
('ARTS_ET_LITTERATURE', 'QCM_4', 'PERSONNE', 'Qui est le sculpteur de la célèbre statue "Le Penseur" ?', 'Auguste Rodin', 'Michel-Ange', 'Camille Claudel', 'Donatello', NULL),
('ARTS_ET_LITTERATURE', 'QCM_4', 'PERSONNE', 'Quel compositeur allemand, devenu sourd, a composé la Neuvième Symphonie ?', 'Ludwig van Beethoven', 'Johann Sebastian Bach', 'Richard Wagner', 'Johannes Brahms', NULL),
('ARTS_ET_LITTERATURE', 'QCM_2', 'CONCEPT', 'Vrai ou Faux : Le mouvement littéraire du Romantisme valorise l''expression des sentiments.', 'Vrai', 'Faux', NULL, NULL, NULL),
('ARTS_ET_LITTERATURE', 'QCM_4', 'OEUVRE', 'De quel conte populaire le personnage de Gepetto est-il issu ?', 'Pinocchio', 'Cendrillon', 'Blanche-Neige', 'Peter Pan', NULL),
('ARTS_ET_LITTERATURE', 'QCM_4', 'PERSONNE', 'Quelle célèbre détective anglaise a été créée par Agatha Christie ?', 'Miss Marple', 'Jessica Fletcher', 'Enola Holmes', 'Nancy Drew', NULL);