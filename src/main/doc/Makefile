PKG = ormlite
DOCS = $(PKG).info $(PKG).html $(PKG) $(PKG).pdf
TEXI = $(PKG).texi
REMOTE ?= outside.256.com

all : $(DOCS)
	cp $(PKG)/* $(PKG).pdf ../javadoc/doc-files

clean :
	rm -rf $(DOCS)

$(PKG).html : $(TEXI)
	rm -f $@
	texi2html -number -monolithic $(TEXI)

$(PKG) : $(TEXI)
	rm -rf $(PKG)
	texi2html --number --split=chapter ormlite.texi

$(PKG).pdf : $(TEXI)
	scp $(TEXI) $(REMOTE):$(PKG)
	ssh $(REMOTE) "(cd $(PKG) ; make)"
	scp $(REMOTE):$(PKG)/ormlite.pdf .

$(PKG).info : $(TEXI)
	rm -f $@
	makeinfo --no-split $(TEXI)
