{{/*
Expand the name of the chart.
*/}}
{{- define "mungcle.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "mungcle.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
Common namespace
*/}}
{{- define "mungcle.namespace" -}}
{{- .Values.global.namespace }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "mungcle.labels" -}}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Selector labels for a given component
Usage: {{ include "mungcle.selectorLabels" (dict "component" "identity") }}
*/}}
{{- define "mungcle.selectorLabels" -}}
app.kubernetes.io/name: {{ .component }}
app.kubernetes.io/part-of: mungcle
{{- end }}

{{/*
Build a full image reference respecting the global registry.
Usage: {{ include "mungcle.image" (dict "registry" .Values.global.image.registry "image" "mungcle/identity" "tag" "latest") }}
*/}}
{{- define "mungcle.image" -}}
{{- if .registry -}}
{{ .registry }}/{{ .image }}:{{ .tag }}
{{- else -}}
{{ .image }}:{{ .tag }}
{{- end -}}
{{- end }}
