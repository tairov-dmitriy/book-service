create table if not exists books(
  id serial primary key,
  name text not null,
  publicationYear int not null,
  annotation text not null
);

create table if not exists authors(
  id serial primary key,
  fullName text not null,
  birthYear int not null
);

create table if not exists books_authors(
  book_id int references books(id) on delete cascade,
  author_id int references authors(id) on delete cascade
);