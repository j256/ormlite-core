PKG = ormlite
DOCS = $(PKG).html $(PKG) $(PKG).pdf
TEXI = $(PKG).texi
REMOTE ?= doc-host
REMOTE_DIR = remote_texi

all : $(DOCS)

install : $(DOCS)
	cp $(PKG)/* $(PKG).pdf ../javadoc/doc-files

clean :
	rm -rf $(DOCS)

$(PKG).html : $(TEXI)
	rm -f $@
	texi2html -number -monolithic $(TEXI)

$(PKG) : $(TEXI)
	rm -rf $(PKG)
	mkdir $(PKG)
	cd $(PKG) ; texi2html --number --split=chapter ../ormlite.texi

$(PKG).pdf : $(TEXI)
	scp $(TEXI) $(REMOTE):$(REMOTE_DIR)
	ssh $(REMOTE) "(cd $(REMOTE_DIR) ; make $@)"
	scp $(REMOTE):$(REMOTE_DIR)/ormlite.pdf .

$(PKG).info : $(TEXI)
	rm -f $@
	makeinfo --no-split $(TEXI)
