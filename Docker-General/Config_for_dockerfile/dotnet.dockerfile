 FROM mcr.microsoft.com/dotnet/aspnet:${version} AS base
WORKDIR /app
EXPOSE 80
EXPOSE 443

FROM mcr.microsoft.com/dotnet/sdk:${version} AS build
WORKDIR /src
COPY ["${csprojFilename}/${csprojFilename}.csproj", "${csprojFilename}/"]
RUN dotnet --version
RUN dotnet restore "${csprojFilename}/${csprojFilename}.csproj"
COPY . .
WORKDIR "/src/${csprojFilename}"
RUN dotnet build "${csprojFilename}.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "${csprojFilename}.csproj" -c Release -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "${csprojFilename}.dll"]