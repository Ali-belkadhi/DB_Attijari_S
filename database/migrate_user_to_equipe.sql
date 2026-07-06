-- Migration vers le modèle : un utilisateur appartient à une seule équipe.
-- À exécuter manuellement sur Oracle avant de redémarrer l'application.
-- Le script s'arrête si un utilisateur appartient à plusieurs équipes afin
-- d'éviter une perte de données silencieuse.

DECLARE
    v_column_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_column_count
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'APP_USERS'
       AND COLUMN_NAME = 'ID_EQUIPE';

    IF v_column_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE APP_USERS ADD (ID_EQUIPE NUMBER(19))';
    END IF;
END;
/

DECLARE
    v_table_count NUMBER;
    v_multiple_memberships NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_table_count
      FROM USER_TABLES
     WHERE TABLE_NAME = 'EQUIPE_MEMBERS';

    IF v_table_count > 0 THEN
        SELECT COUNT(*)
          INTO v_multiple_memberships
          FROM (
              SELECT USER_ID
                FROM EQUIPE_MEMBERS
               GROUP BY USER_ID
              HAVING COUNT(DISTINCT ID_EQUIPE) > 1
          );

        IF v_multiple_memberships > 0 THEN
            RAISE_APPLICATION_ERROR(
                -20001,
                'Migration annulée : au moins un utilisateur appartient à plusieurs équipes.'
            );
        END IF;

        EXECUTE IMMEDIATE q'[
            MERGE INTO APP_USERS u
            USING (
                SELECT USER_ID, MAX(ID_EQUIPE) AS ID_EQUIPE
                  FROM EQUIPE_MEMBERS
                 GROUP BY USER_ID
            ) em
               ON (u.ID_USER = em.USER_ID)
            WHEN MATCHED THEN
                UPDATE SET u.ID_EQUIPE = em.ID_EQUIPE
                 WHERE u.ID_EQUIPE IS NULL
        ]';
    END IF;
END;
/

DECLARE
    v_constraint_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_constraint_count
      FROM USER_CONSTRAINTS c
      JOIN USER_CONS_COLUMNS cc
        ON cc.CONSTRAINT_NAME = c.CONSTRAINT_NAME
       AND cc.TABLE_NAME = c.TABLE_NAME
     WHERE c.TABLE_NAME = 'APP_USERS'
       AND c.CONSTRAINT_TYPE = 'R'
       AND cc.COLUMN_NAME = 'ID_EQUIPE';

    IF v_constraint_count = 0 THEN
        EXECUTE IMMEDIATE '
            ALTER TABLE APP_USERS
            ADD CONSTRAINT FK_APP_USERS_EQUIPE
            FOREIGN KEY (ID_EQUIPE) REFERENCES EQUIPES(ID_EQUIPE)
        ';
    END IF;
END;
/

DECLARE
    v_column_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_column_count
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'APP_USERS'
       AND COLUMN_NAME = 'DEPARTEMENT_ID';

    IF v_column_count > 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE APP_USERS DROP COLUMN DEPARTEMENT_ID';
    END IF;
END;
/

-- EQUIPE_MEMBERS est volontairement conservée comme sauvegarde de migration.
-- Après validation des données, elle peut être supprimée manuellement :
-- DROP TABLE EQUIPE_MEMBERS CASCADE CONSTRAINTS;
