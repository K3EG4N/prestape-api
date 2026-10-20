-- Datos de desarrollo de Prestape.
-- Ejecutar DESPUÉS de que Flyway haya aplicado las migraciones (arrancar el backend una vez).
-- Vacía las tablas (incluidos los roles que inserta la V1) y carga los datos con sus ids originales.

TRUNCATE TABLE menu_roles, user_roles, people, menus, users, roles RESTART IDENTITY CASCADE;

--
-- PostgreSQL database dump
--

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: menus; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.menus (id, name, "position", url, icon, created_at, updated_at, deleted_at) VALUES (2, 'Menú Principal', 1, '/', 'home', '2026-10-11 21:24:16.354987-05', '2026-10-11 21:24:16.354987-05', NULL);
INSERT INTO public.menus (id, name, "position", url, icon, created_at, updated_at, deleted_at) VALUES (1, 'Usuarios', 2, '/users', 'users', '2026-10-11 21:24:16.354987-05', '2026-10-11 21:24:16.354987-05', NULL);
INSERT INTO public.menus (id, name, "position", url, icon, created_at, updated_at, deleted_at) VALUES (3, 'Catálogos', 3, '/catalog', 'catalog', '2026-10-11 21:24:16.354987-05', '2026-10-11 21:24:16.354987-05', NULL);


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.roles (id, name, created_at, updated_at, deleted_at, external_name) VALUES (1, 'ADMIN', '2026-10-01 20:50:39.05748-05', '2026-10-01 20:50:39.05748-05', NULL, 'ADMINISTRADOR');
INSERT INTO public.roles (id, name, created_at, updated_at, deleted_at, external_name) VALUES (2, 'STUDENT', '2026-10-01 20:50:39.05748-05', '2026-10-01 20:50:39.05748-05', NULL, 'ESTUDIANTE');
INSERT INTO public.roles (id, name, created_at, updated_at, deleted_at, external_name) VALUES (3, 'TEACHER', '2026-10-01 20:50:39.05748-05', '2026-10-01 20:50:39.05748-05', NULL, 'DOCENTE');
INSERT INTO public.roles (id, name, created_at, updated_at, deleted_at, external_name) VALUES (4, 'SUPERVISOR', '2026-10-01 20:50:39.05748-05', '2026-10-01 20:50:39.05748-05', NULL, 'SUPERVISOR');


--
-- Data for Name: menu_roles; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.menu_roles (menu_id, role_id) VALUES (1, 1);
INSERT INTO public.menu_roles (menu_id, role_id) VALUES (2, 1);
INSERT INTO public.menu_roles (menu_id, role_id) VALUES (3, 1);
INSERT INTO public.menu_roles (menu_id, role_id) VALUES (2, 3);
INSERT INTO public.menu_roles (menu_id, role_id) VALUES (2, 2);


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.users (id, username, email, password, created_at, updated_at, deleted_at, profile_completed, status) VALUES (12, NULL, 'felipe.espinoza@prestape.com', '$2a$10$yhn6JJAWksS7WD5zsgfjde8S79X5/Fq53hUFuxykbj8nw8Qto1VOG', '2026-10-08 22:50:15.484634-05', '2026-10-08 22:50:15.484634-05', NULL, true, 'ACTIVE');
INSERT INTO public.users (id, username, email, password, created_at, updated_at, deleted_at, profile_completed, status) VALUES (14, NULL, 'talita.carolay@prestape.com', '$2a$10$LOVTTkca/wIVOIKWtrGs4eTNv.68lI5kSIpL0obYfn..oVBScfyQq', '2026-10-08 23:03:34.182226-05', '2026-10-08 23:03:34.182226-05', NULL, true, 'ACTIVE');
INSERT INTO public.users (id, username, email, password, created_at, updated_at, deleted_at, profile_completed, status) VALUES (17, NULL, 'piero.quispe@prestape.com', '$2a$10$cnnyb1jB/jt2otwbUwWHeeEdpmhI6NB2hUKsd32PtmNP5pf9469qy', '2026-10-18 00:42:51.631256-05', '2026-10-18 00:42:51.660303-05', NULL, true, 'ACTIVE');
INSERT INTO public.users (id, username, email, password, created_at, updated_at, deleted_at, profile_completed, status) VALUES (11, 'admin', 'admin@prestape.com', '$2a$10$0orfaP3CL6z48M3Bx8eZFuLpCMw/t4a4rJUa8FyHPJlOQBJ.IKEh.', '2026-10-04 00:21:20.106827-05', '2026-10-04 00:21:20.106827-05', '2026-10-18 01:13:10.239-05', false, 'ACTIVE');
INSERT INTO public.users (id, username, email, password, created_at, updated_at, deleted_at, profile_completed, status) VALUES (20, NULL, 'luis.nunez@prestape.com', '$2a$10$VOHh4wvHQWoduHW0aOwN8.NXqTd/qfJJ7zYpZX7ONBruGelOC/N66', '2026-10-18 00:59:20.793627-05', '2026-10-20 09:56:19.167509-05', NULL, true, 'SUSPENDED');
INSERT INTO public.users (id, username, email, password, created_at, updated_at, deleted_at, profile_completed, status) VALUES (21, NULL, 'marco.perez@prestape.com', '$2a$10$oVd30bZETXsy70h/T8lIN.uAh2ZiTlvt/GONgUrQIjNv5Nucqj/WK', '2026-10-20 10:46:16.765155-05', '2026-10-20 11:10:30.112974-05', NULL, true, 'ACTIVE');


--
-- Data for Name: people; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.people (id, code, document_number, first_name, middle_name, paternal_surname, maternal_surname, phone, user_id, created_at, updated_at, deleted_at) VALUES (2, 'P00002', '72727320', 'Talita', 'Carolay', 'Estela', NULL, '933578032', 14, '2026-10-11 15:00:14.854-05', '2026-10-11 15:00:14.854-05', NULL);
INSERT INTO public.people (id, code, document_number, first_name, middle_name, paternal_surname, maternal_surname, phone, user_id, created_at, updated_at, deleted_at) VALUES (3, 'XG539R', '06654331', 'Piero', NULL, 'Quispe', 'Villegas', '999120669', 17, '2026-10-18 00:42:51.647271-05', '2026-10-18 00:42:51.647271-05', NULL);
INSERT INTO public.people (id, code, document_number, first_name, middle_name, paternal_surname, maternal_surname, phone, user_id, created_at, updated_at, deleted_at) VALUES (4, 'ADEAXU', '09341413', 'Luis', 'Cesar', 'Nuñez', 'Ramos', '994662059', 20, '2026-10-18 00:59:20.804001-05', '2026-10-18 00:59:20.804001-05', NULL);
INSERT INTO public.people (id, code, document_number, first_name, middle_name, paternal_surname, maternal_surname, phone, user_id, created_at, updated_at, deleted_at) VALUES (5, 'VPD5C5', '06354895', 'Marco', 'Antonio', 'Perez', 'Mendoza', '999120684', 21, '2026-10-20 11:10:30.09867-05', '2026-10-20 11:10:30.09867-05', NULL);
INSERT INTO public.people (id, code, document_number, first_name, middle_name, paternal_surname, maternal_surname, phone, user_id, created_at, updated_at, deleted_at) VALUES (1, 'P00001', '74967544', 'Juan', 'Felipe', 'Espinoza', 'Huarcaya', '957933670', 12, '2026-10-11 15:00:14.854-05', '2026-10-20 11:13:48.905037-05', NULL);


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.user_roles (user_id, role_id) VALUES (11, 1);
INSERT INTO public.user_roles (user_id, role_id) VALUES (14, 3);
INSERT INTO public.user_roles (user_id, role_id) VALUES (17, 2);
INSERT INTO public.user_roles (user_id, role_id) VALUES (20, 2);
INSERT INTO public.user_roles (user_id, role_id) VALUES (12, 1);
INSERT INTO public.user_roles (user_id, role_id) VALUES (21, 2);


--
-- Name: menu_items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.menu_items_id_seq', 3, true);


--
-- Name: people_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.people_id_seq', 5, true);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.roles_id_seq', 4, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_id_seq', 21, true);


--
-- PostgreSQL database dump complete
--


