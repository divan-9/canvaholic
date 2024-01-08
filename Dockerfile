FROM babashka/babashka:1.3.186-alpine

COPY . /src
WORKDIR /src
ENTRYPOINT ["bb", "-m", "canvaholic.main"]
