from docx import Document
path="docxs/USABO 17 Semifinal Final Key web.docx"
doc=Document(path)

for par in doc.paragraphs:
    highlighted=""
    for run in par.runs:
        print(run.font.highlight_color, run.text)
        input("> ")
        if run.font.highlight_color:
            highlight+=run.text
    if highlighted:
        print(highlighted)


