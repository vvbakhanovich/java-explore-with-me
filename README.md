# java-explore-with-me

Данное приложение - _афиша_, позволяет пользователям делиться информацией об интересных событиях и находить компанию для
участия в них.

_Используемый стэк_: Spring Boot, Spring Data JPA, PostgreSQL, Lombok, JUnit, Mockito, Mapstruct, Docker, Docker Compose.

---

Приложение разбито на два сервиса:
1. _Сервис сбора статистики (stats)_
    - Сохраняет количество обращений по эндпоинтам;
    - Позволяет делать различные выборки для анализа работы приложения.
2. _Основной сервис_

---

API основного сервиса разделена на три части:
- публичная доступна без регистрации любому пользователю сети;
- закрыта доступна только авторизованным пользователям;
- административная — для администраторов сервиса.

_Публичный API_ должен представляет возможности поиска и фильтрации событий. Основные моменты:
- сортировка списка событий возможна по количеству просмотров, которое будет запрашиваться в сервисе статистики,
  по датам событий, а также по количеству комментариев под событием;
- при просмотре списка событий возвращается только краткая информация о мероприятиях;
- просмотр подробной информации о конкретном событии осуществляется через отдельный эндпоинт;
- есть возможность получения всех имеющихся категорий и подборок событий, которые составляют администраторы ресурса.

_Закрытая часть API_ реализует возможности зарегистрированных пользователей продукта.
- авторизованные пользователи имеют возможность добавлять в приложение новые мероприятия, редактировать их,
- просматривать после добавления, оставлять комментарии и оставлять заявки на участие;
- создатель мероприятия может подтверждать заявки, которые отправили другие пользователи сервиса.

_Административная часть API_ предоставляет возможности настройки и поддержки работы сервиса. Основной функционал:
- добавление, изменение и удаление категорий для событий;
- возможность добавлять, удалять и закреплять на главной странице подборки мероприятий;
- модерация событий, размещённых пользователями, — публикация или отклонение;
- управление пользователями — добавление, активация, просмотр и удаление.

---

_Жизненный цикл события_ включает несколько этапов.
1. Создание;
2. Ожидание публикации. В статус ожидания публикации событие переходит сразу после создания;
3. Публикация. В это состояние событие переводит администратор;
4. Отмена публикации. В это состояние событие переходит в двух случаях. Первый — если администратор решил, что его
   нельзя публиковать. Второй — когда инициатор события решил отменить его на этапе ожидания публикации.
   
---

Приложение написано на Java 11 с использованием Spring Boot 2.7.5. Используемая база данных - PostgreSQL.

---

__TODO__: Добавить Spring Security для авторизации и аутентицифакции пользователей.
