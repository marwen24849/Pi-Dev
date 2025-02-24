2025-02-20 10:04:32 
SELECT r.id, r.quiz_id, r.resultat_id, r.user_id, rr.answer, rr.question
FROM response r
JOIN response_responses rr ON r.id = rr.response_id
WHERE r.id = (SELECT MAX(id) FROM response);
******************************************** ********************************************
2025-02-20 10:07:59 
 INSERT INTO question (category, difficultylevel, option1, option2, option3, option4, question_title, right_answer, score)
VALUES ('Nouvelle catégorie', 'Niveau difficile', 'Option 1', 'Option 2', 'Option 3', 'Option 4', 'Nouvelle question', 'Option 1', 10);
******************************************** ********************************************
2025-02-20 10:16:47 
 SELECT MAX(id) AS dernier_question
FROM question;
******************************************** ********************************************
2025-02-21 11:14:12 
 SELECT COUNT(*) AS nombre_questions
FROM question;
******************************************** ********************************************
2025-02-21 11:18:47 
 SELECT COUNT(*) AS nombre_de_questions
FROM question;
******************************************** ********************************************
2025-02-21 11:22:06 
 SELECT r.id, r.quiz_id, r.resultat_id, r.user_id, rr.answer, rr.question
FROM response r
JOIN response_responses rr ON r.id = rr.response_id
WHERE r.resultat_id = (SELECT id FROM resultat WHERE resultat = 1 ORDER BY score DESC LIMIT 1)
ORDER BY r.id;
******************************************** ********************************************
2025-02-21 11:37:12 
 SELECT COUNT(*) AS nombre_de_quiz
FROM quiz;
******************************************** ********************************************
2025-02-21 11:44:25 
 SELECT COUNT(*) AS nombre_de_questions
FROM question;
******************************************** ********************************************
2025-02-21 11:44:33 
 SELECT COUNT(*) AS nombre_de_quiz
FROM quiz;
******************************************** ********************************************
2025-02-21 15:39:47 
 SELECT MAX(LENGTH(content)) AS max_length
FROM chat;
******************************************** ********************************************
2025-02-21 15:40:07 
 SELECT content 
FROM chat 
ORDER BY LENGTH(content) DESC 
LIMIT 1;
******************************************** ********************************************
2025-02-22 17:15:34 
 SELECT 
  q.id, 
  q.question_title, 
  q.difficultylevel, 
  q.category, 
  q.score, 
  q.right_answer, 
  q.option1, 
  q.option2, 
  q.option3, 
  q.option4, 
  qz.id AS quiz_id, 
  qz.title AS quiz_title, 
  qz.difficultylevel AS quiz_difficultylevel, 
  qz.category AS quiz_category, 
  qz.minimum_success_percentage, 
  qz.passer
FROM 
  question q
JOIN 
  quiz_questions qq ON q.id = qq.questions_id
JOIN 
  quiz qz ON qq.quiz_id = qz.id
ORDER BY 
  q.id;
******************************************** ********************************************
2025-02-24 12:37:29 
 SELECT *
FROM resultat;
******************************************** ********************************************
2025-02-24 12:41:38 
 SELECT COUNT(*) AS nombre_des_questions
FROM question;
******************************************** ********************************************
2025-02-24 12:54:21 
 INSERT INTO question (category, difficultylevel, option1, option2, option3, option4, question_title, right_answer, score)
VALUES ('Catégorie', 'Niveau', 'Option 1', 'Option 2', 'Option 3', 'Option 4', 'Titre de la question', 'Réponse correcte', 10);
******************************************** ********************************************
2025-02-24 13:10:45 
 SELECT COUNT(*) AS nombre_quiz
FROM quiz;
******************************************** ********************************************
