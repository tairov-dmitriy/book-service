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

create table if not exists customers(
  id serial primary key,
  name text not null,
  phone text not null
);

create table if not exists orders(
  id serial primary key,
  customer_id int references customers(id) on delete cascade,
  creationDate date not null,
  completeDate date,
  completed boolean not null
);

create table if not exists orders_books(
  order_id int references orders(id) on delete cascade,
  book_id int references books(id) on delete cascade
);