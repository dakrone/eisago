CREATE EXTENSION "uuid-ossp";

CREATE TABLE project (
       id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
       name text NOT NULL
);

CREATE TABLE namespace (
       id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
       project_id uuid NOT NULL REFERENCES project(id),
       name text NOT NULL
);

CREATE TABLE var (
       id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
       namespace_id uuid NOT NULL REFERENCES namespace(id),
       name text NOT NULL
);

CREATE TABLE var_attribute (
       id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
       var_id uuid NOT NULL REFERENCES var(id)
);

CREATE TABLE var_version (
       version text,
       doc text,
       source text
) INHERITS (var_attribute);

CREATE TABLE var_comment (
       body text NOT NULL
) INHERITS (var_attribute);

CREATE TABLE var_example (
       body text NOT NULL
) INHERITS (var_attribute);

