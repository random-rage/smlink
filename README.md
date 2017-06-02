# smlink
SPO Machine Linker

Компоновщик для модели вычислительной машиный [SPOM](https://github.com/random-rage/spom).

## Использование

`smlink <-s|-d> <-l|-e> <Имя файла без расширения> [Путь к папке с библиотеками]`

Ключ `-s` указывает, что нужно произвести статическую компоновку, `-d` - динамическую.

Ключ `-l` указывает, что нужно создать библиотеку, `-e` - исполняемый файл (по сути, отличаются только расширением файла).

Подробнее: [wiki](https://github.com/random-rage/spom/wiki).

### Примеры

Для статической компоновки и создания исполняемого файла:

`smlink -s -e source ./libs`

Для статической компоновки и создания библиотеки:

`smlink -s -l lib`

Для динамической компоновки и создания исполняемого файла:

`smlink -d -e source`

Для динамической компоновки и создания библиотеки:

`smlink -d -l lib`
