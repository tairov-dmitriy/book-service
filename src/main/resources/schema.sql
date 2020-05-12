create table if not exists books(
  id serial primary key,
  name text not null,
  publicationYear int not null,
  annotation text not null
);

create index if not exists id_book_names on books(name);

create table if not exists authors(
  id serial primary key,
  fullName text not null,
  birthYear int not null
);

create index if not exists id_author_fullName on authors(fullName);

create table if not exists books_authors(
  book_id int references books(id) on delete cascade,
  author_id int references authors(id) on delete cascade
);