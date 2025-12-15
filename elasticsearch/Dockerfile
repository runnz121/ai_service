FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.1

# Install nori plugin for Korean text analysis
RUN bin/elasticsearch-plugin install analysis-nori

# Expose Elasticsearch ports
EXPOSE 9200 9300
