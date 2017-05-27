FROM clojure
RUN mkdir -p /opt/app
WORKDIR /opt/app
COPY project.clj /opt/app/
RUN lein deps
COPY . /opt/app
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar
CMD ["java", "-jar", "app-standalone.jar"]
