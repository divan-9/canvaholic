# canvaholic/canvaholic

This program is aimed to generate SVG basing on Obsidian Canvas

## Usage

### From source code

```
cat sample.canvas | bb -m canvaholic.main
```

### From docker

```
cat sample.canvas | docker run -i divan9/canvaholic:0
```

## Docker build

```
 docker build . -t divan9/canvaholic:0 --platform linux/arm64/v8
 docker push divan9/canvaholic:0
```
