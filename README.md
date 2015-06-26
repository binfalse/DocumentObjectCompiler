# DocumentObjectCompiler
The DocumentObjectCompiler is a tool to create a **PDF** given a **Document Object.**

## Build
You can build the tool using Maven

    git clone https://github.com/binfalse/DocumentObjectCompiler
    cd DocumentObjectCompiler
    mvn clean package

Now you'll see a `target` directory containing the binaries. The file `DocumentObjectCompiler-$VERSION-jar-with-dependencies.jar` will have all the dependencies included, ready to be used. 

## Usage
To get a PDF out of a Document Object you just need to pass the Document Object as a parameter. You can find an example Document Object in the `test/` directory:

    java -jar target/DocumentObjectCompiler-$VERSION-jar-with-dependencies.jar test/do.bundle

If that was successful the final PDF along with some `pdflatex` output can be found next to the provided Document Object.

## Document Object?
A Document Object is a [Research Object](http://www.researchobject.org/) containing a project that is meant to represent a document. Research Objects are powerful files that are able to encode and transfer reproducible jobs/research/results.

